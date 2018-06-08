<div align="center"> 
    <!--<img width="500px" alt="BookLab Logo">-->
    <br> 
    <h1>BookLab</h1> 
    Finding The Right Book On Another Shelf
</div>

## Introduction
BookLab allows users to quickly scan the books on their bookshelves and provides
recommendations based on your collection when scanning other bookshelves.

## Quick Start

### Getting the source
Download the source code by running the following code in your command prompt:
```sh
git clone https://gitlab.ewi.tudelft.nl/TI2806/2017-2018/MS/ms1/ms1.git
```
or simply 
[grab](https://gitlab.ewi.tudelft.nl/TI2806/2017-2018/MS/ms1/ms1/-/archive/master/ms1-master.zip) 
a copy of the source code as a Zip file.

### Deploy (with Docker)
Make sure you have done the following:
- **Install and start Docker**  
  Docker should be running before you are able to build the images. See [Docker](https://docker.com) for more information
  on installing it. On Windows, make sure you have exposed the daemon without TLS (you can change this in Settings).
- **Install docker-compose**  
  Make sure that `docker-compose` is installed on your system. See [this page](https://docs.docker.com/compose/install/)
  on how to install the program.
- **Copy Google Cloud credentials to configuration folder**  
  The key file for your Google Cloud service account should be put in the `config` directory right in the root of the
  project as `keys.json`.
  See [this page](https://cloud.google.com/docs/authentication/getting-started) on how to acquire a service account for
  Google Cloud and generate a key file. Make sure you have enabled the Google Vision API.

After that, run the following command to build the Docker images for the project:
```sh
./gradlew dockerBuildImage
```

Finally, you can start the images using `docker-compose`. Make sure that you have set the `TAG` environmental variable
to the version you want to deploy (e.g 1.0).
```sh
docker-compose up
```

The website will now be available at [http://localhost](http://localhost).
### Deploy (without Docker)
Make sure the following variables are defined in your environment:
- **GOOGLE_BOOKS_API_KEY**   
  An API key for Google Books which allows the backend server to query its catalogue. See [this page](https://developers.google.com/books/docs/v1/using#APIKey)
  on how to acquire an API key.
- **GOOGLE_APPLICATION_CREDENTIALS**
  The path to your Google Cloud service account keys. See [this page](https://cloud.google.com/docs/authentication/getting-started) on
  how to acquire a service account for Google Cloud and generate a key file. Make sure you have enabled the Google Vision API.

After that, run the following command to start the backend:
```sh
./gradlew run
```

To run the frontend run the following code in your command prompt within the `booklab-frontend` 
folder. Make sure you have installed the dependencies of the project using `npm install`.
```sh
npm run ng serve -- --open
```
This will automatically open the frontend on [http://localhost:4200](http://localhost:4200).

## Development
Before starting development, make sure you have the correct environmental variables defined. See
the deployment steps on which variables have to be defined.

### Backend
In order to run the tests and the linter, enter the following code in your command prompt:
```sh
./gradlew check
```
This command will show all the tests that fail and possible formatting errors.

### Frontend (Angular)
In order to run the tests for the Angular frontend, enter the following code in your command prompt in the `booklab-frontend`
directory:
```sh
npm run test
```

Additionally, if you want to check the source code for formatting errors, run:
```sh 
npm run lint
```

Finally, if you want to run the end-to-end tests, run:
```sh 
npm run e2e
```

### Building without mobile applications
If you do not have the Android SDK or iOS SDK installed and you are not interested in building
the mobile applications, either:

- Set the environmental variable to ignore the projects:  
  `ORG_GRADLE_PROJECT_exclude=booklab-frontend-ios,booklab-frontend-android`
- Set the `exclude` property in your `gradle.properties`:  
  `exclude=booklab-frontend-ios,booklab-frontend-android`


## Contributing
In addition to this document, the repository contains a README for each of the modules that builds this project. 
Read below to learn how you can take part in improving BookLab.

### Contributing Guide
Read our [contributing guide](CONTRIBUTING.md) to learn about our development process, how to propose bugfixes and 
improvements, and how to build and test your changes to BookLab.

### License
The code is released under the Apache version 2.0 license. See the 
`LICENSE.txt` file.


