<div align="center"> 
    <!--<img width="500px" alt="BookLab Logo">-->
    <br> 
    <h1>BookLab</h1> 
    Finding The Right Book On Another Shelf
</div>

## Introduction
BookLab allows users to quickly scan the books on their bookshelves and provides
recommendations based on your collection when scanning other bookshelves.

## Getting the source
Download the source code by running the following code in your command prompt:
```sh
$ git clone https://gitlab.ewi.tudelft.nl/TI2806/2017-2018/MS/ms1/ms1.git
```
or simply [grab](https://gitlab.ewi.tudelft.nl/TI2806/2017-2018/MS/ms1/ms1/-/archive/master/ms1-master.zip) 
a copy of the source code as a Zip file.

## Building
For building the source code, we use the Gradle build system. To build the 
system, enter the following in your command prompt:
```sh
$ ./gradlew build
```
To run the test code, run the following code in your command prompt:
```sh
$ ./gradlew test
```
## Working with Angular
To install Angular globally run the following code in your command prompt:
```sh
$ npm install -g @angular/cli
````
To install dependencies run the following code within booklab-frontend:
```
$npm install
```

To run the website run the following code in your command prompt:
```sh
ng serve --open
```

## License
The code is released under the Apache version 2.0 license. See the 
`LICENSE.txt` file.


