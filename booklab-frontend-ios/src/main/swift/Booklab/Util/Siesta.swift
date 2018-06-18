//
//  Siesta+RxCocoa.swift
//  Siesta
//
//  Created by Stas Chmilenko on 18.01.17.
//  Copyright © 2017 Bust Out Solutions. All rights reserved.
//

import Siesta
import RxSwift
import RxCocoa


// MARK: - Resource extension
// MARK: Basic rx functionality
extension Reactive where Base: Resource
{
    /// Creates observable returning latest state of resource end event changed it.
    public var changes: Driver<(resource: Resource, event: ResourceEvent)>
    {
        return createObservable().map { (resource: $0, event: $1) }
            .asDriver(onErrorJustReturn: (resource: self.base, event: .error))
    }
    
    /// Creates observable returning latestData.
    public var latestData: Driver<Entity<Any>?>
    {
        return createObservable().map { $0.0.latestData }
            .asDriver(onErrorJustReturn: nil)
    }
    
    /// Creates observable returning latestError.
    public var latestError: Driver<RequestError?>
    {
        return createObservable().map { $0.0.latestError }
            .asDriver(onErrorJustReturn: nil)
    }
    
    /// Creates observable returning isLoading.
    public var isLoading: Driver<Bool>
    {
        return createObservable().map { $0.0.isLoading }
            .asDriver(onErrorJustReturn: false)
    }
    
    /// Creates observable returning isRequesting.
    public var isRequesting: Driver<Bool>
    {
        return createObservable().map { $0.0.isRequesting }
            .asDriver(onErrorJustReturn: false)
    }
    
    /// Creates observable listening for resource changes and removing listener on disposing of it.
    private func createObservable() -> Observable<(Resource, ResourceEvent)>
    {
        return Observable.create { observer in
            let owner = ObserverOwner()
            self.base.addObserver(owner: owner) { r, e in observer.onNext((r, e)) }
            return Disposables.create {
                self.base.removeObservers(ownedBy: owner)
            }
        }
    }
}

/// Class used as plaseholder for resource observer owner
private class ObserverOwner {}
// MARK: ObservableTypedContentAccessors
/**
 Brings functionality of TypedContentAccessors to Rx extension of Resource.
 I couldn't figure out how to write it as protocol because expressions like
 extension Reactive: ObservableTypedContentAccessors where Base: Resource
 not supported
 */
public extension Reactive where Base: Resource {
    public func content<T : Codable>() -> Observable<T> {
        return createObservable()
            .map { (resource, event) in
                switch event {
                case .requested, .observerAdded:
                    return nil
                default:
                    if let data = resource.latestData?.content as? BackendResponse<T> {
                        return data
                    } else if let error = resource.latestError?.entity?.content as? BackendResponse<T> {
                        return error
                    } else if let error = resource.latestError?.cause {
                        throw error
                    } else if let error = resource.latestError {
                        throw error
                    } else {
                        let error = BackendError(
                            code: "invalid_response",
                            title: "Failed to convert the reponse into the appropriate object.",
                            detail: nil
                        )
                        return .Failure(error, [:],  [:])
                    }
                }
            }
            .filter { (res: BackendResponse<T>?) in res != nil }
            .map { (res: BackendResponse<T>?) in
                switch res! {
                case .Success(let data, _, _):
                    return data
                case .Failure(let error, _, _):
                    throw error
                }
            }
    }
}
