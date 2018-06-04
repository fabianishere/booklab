// Karma configuration file, see link for more information
// https://karma-runner.github.io/1.0/config/configuration-file.html

// Use Chrome's puppeteer if available
try {
    process.env.CHROME_BIN = require('puppeteer').executablePath();
} catch (e) {
}

module.exports = function (config) {
    config.set({
        basePath:  __dirname,
        frameworks: ['jasmine', '@angular-devkit/build-angular'],
        plugins: [
            require('karma-jasmine'),
            require('karma-chrome-launcher'),
            require('karma-jasmine-html-reporter'),
            require('karma-coverage-istanbul-reporter'),
            require('@angular-devkit/build-angular/plugins/karma'),
            require('karma-verbose-reporter'),
            require('karma-html-reporter')
        ],
        client: {
            clearContext: false // leave Jasmine Spec Runner output visible in browser
        },
        coverageIstanbulReporter: {
            dir: require('path').join(__dirname, 'build/reports/istanbul'),
            reports: ['html', 'lcovonly', 'text-summary'],
            fixWebpackSourcePaths: true
        },
        htmlReporter: {
            outputDir: 'build/reports/karma',
            urlFriendlyName: true
        },
        angularCli: {
            environment: 'dev'
        },
        reporters: ['verbose', 'html', 'kjhtml'],
        port: 9876,
        colors: true,
        logLevel: config.LOG_INFO,
        autoWatch: true,
        browsers: ['ChromeHeadless'],
        customLaunchers: {
            ChromeCI: {
                base: 'ChromeHeadless',
                flags: ['--no-sandbox', '--disable-setuid-sandbox']
            }
        }
    });
};
