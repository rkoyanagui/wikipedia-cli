# wikipedia-cli

## Minimum requirements

* JRE 17 or greater

## How to use

First, you must define a _user agent_, as a courtesy to let Wikipedia know a bot is calling its API. That can be done in
two ways.

You can define an environment variable:

```shell
export USER_AGENT='coolbot/1.0.0 (https://coolbot.com; coolbot@example.org)'
```

Or you can pass it as a command line argument:

```shell
java -jar -Dwikipedia.headers.user-agent="coolbot/1.0.0 (https://coolbot.com; coolbot@example.org)" wikipedia-cli-1.0.0-all.jar --help
```

To check all available commands and how to use them, use `--help`.

## How to build from source

You must have JDK 17 or greater installed.

Clone this repository, then run:

```shell
./gradlew build
```
