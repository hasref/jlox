# jlox

An interpreter for the Lox language, written in Java by following along with Bob Nystrom's ["Crafting Interpreters"](http://craftinginterpreters.com/).

## Building

From the root folder, build using gradle `./gradlew build` and launch using `./gradlew app:run -q --console=plain`.

## Implemented Challenges

While the book itself provides source code for the interpreter, this implementation adds the following suggested features/"challenges" from the book:

- [x] Multiline C-style comments: `/* */`.
- [x] Reverse Polish Notation Printer: (1 + 2) \* (4 - 3) --> 1 2 + 4 3 - \*
- [x] C-style comma operator
- [ ] Error messages show location of error:

```java
Error: Unexpected "," in argument list.

    15 | function(first, second,);
                               ^-- Here.
```

- [ ] Blobs of invalid characters produce a single error message.
