# proto-repl-test

A project to experiment with proto-repl

## Installation

- Follow instructions on [proto-repl github page](https://github.com/jasongilman/proto-repl)
- Setup Atom & Clojure with the instructions in [this gist](https://gist.github.com/jasongilman/d1f70507bed021b48625)
- [Instructional video](https://www.youtube.com/watch?v=BJUI1ntfPy8&feature=youtu.be) by Misophistful

## Keep a trail

- Setup Atom (see previous paragraph)
- Created this leiningen project with `lein new app proto-repl-test`
- add proto-repl dependencies for code completion: [proto-repl "0.3.1"] [proto-repl-charts "0.3.2"]

- Start repl `alt-cmd-L` This start a repl (and loads project dependencies). Repl instructions are given:

```ascii
REPL Instructions

Code can be entered at the bottom and executed by pressing shift+enter.

Try it now by typing (+ 1 1) in the bottom section and pressing shift+enter.

Working in another Clojure file and sending forms to the REPL is the most efficient way to work. Use the following key bindings to send code to the REPL. See the settings for more keybindings.

ctrl-alt-, then b
Execute block. Finds the block of Clojure code your cursor is in and executes that.

ctrl-alt-, s
Executes the selection. Sends the selected text to the REPL.

You can disable this help text in the settings.
Starting REPL with lein in /Users/mmz/Projects/proto-repl-test
```

- Navigate to `core.clj`
- send `ns` block to repl
- send `-main` block to repl
- type `(-main)` and send it to the repl

## License

Use / change / redistribute at your own risk. It is not copyrighted. I'm standing on the shoulders of giants here.
