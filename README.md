# proto-repl-test

A project to experiment with proto-repl

## Installation

- Follow instructions on [proto-repl github page](https://github.com/jasongilman/proto-repl)
- Setup Atom & Clojure with the instructions in [this gist](https://gist.github.com/jasongilman/d1f70507bed021b48625)
- [Instructional video](https://www.youtube.com/watch?v=BJUI1ntfPy8&feature=youtu.be) by Misophistful
- parinfer: Indent (instead of paren)

Package directory after installation:

  - atom-beautify
  - highlight-selected
  - ink
  - lisp-paredit
  - minimap
  - minimap-find-and-replace
  - minimap-highlight-selected
  - parinfer
  - proto-repl
  - proto-repl-charts
  - proto-repl-sayid
  - recent-files-fuzzy-finder
  - set-syntax
  - tool-bar

`init.coffee`:

```
# Your init script
#
# Atom will evaluate this file each time a new window is opened. It is run
# after packages are loaded/activated and after the previous editor state
# has been restored.
#
# An example hack to log to the console when each text editor is saved.
#
# atom.workspace.observeTextEditors (editor) ->
#   editor.onDidSave ->
#     console.log "Saved! #{editor.getPath()}"

# These add some convenience commands for cutting, copying, pasting, deleting, and indenting Lisp expressions.

# Applies the function f and then reverts the cursor positions back to their original location
maintainingCursorPosition = (f)->
  editor = atom.workspace.getActiveTextEditor()
  currSelected = editor.getSelectedBufferRanges()
  f()
  editor.setSelectedScreenRanges(currSelected)

# Cuts the current block of lisp code.
atom.commands.add 'atom-text-editor', 'jason:cut-sexp', ->
  editor = atom.workspace.getActiveTextEditor()
  atom.commands.dispatch(atom.views.getView(editor), 'lisp-paredit:up-sexp')
  atom.commands.dispatch(atom.views.getView(editor), 'lisp-paredit:expand-selection')
  atom.commands.dispatch(atom.views.getView(editor), 'core:cut')

# Copies the current block of lisp code.
atom.commands.add 'atom-text-editor', 'jason:copy-sexp', ->
  maintainingCursorPosition ->
    editor = atom.workspace.getActiveTextEditor()
    atom.commands.dispatch(atom.views.getView(editor), 'lisp-paredit:up-sexp')
    atom.commands.dispatch(atom.views.getView(editor), 'lisp-paredit:expand-selection')
    atom.commands.dispatch(atom.views.getView(editor), 'core:copy')

# Pastes over current block of lisp code.
atom.commands.add 'atom-text-editor', 'jason:paste-sexp', ->
  editor = atom.workspace.getActiveTextEditor()
  atom.commands.dispatch(atom.views.getView(editor), 'lisp-paredit:up-sexp')
  atom.commands.dispatch(atom.views.getView(editor), 'lisp-paredit:expand-selection')
  atom.commands.dispatch(atom.views.getView(editor), 'core:paste')

# Deletes the current block of lisp code.
atom.commands.add 'atom-text-editor', 'jason:delete-sexp', ->
  editor = atom.workspace.getActiveTextEditor()
  atom.commands.dispatch(atom.views.getView(editor), 'lisp-paredit:up-sexp')
  atom.commands.dispatch(atom.views.getView(editor), 'lisp-paredit:expand-selection')
  atom.commands.dispatch(atom.views.getView(editor), 'core:delete')

# Indents the top level sexp.
atom.commands.add 'atom-text-editor', 'jason:indent-top-sexp', ->
  maintainingCursorPosition ->
    editor = atom.workspace.getActiveTextEditor()
    range = protoRepl.EditorUtils.getCursorInClojureTopBlockRange(editor)
    # Work around a lisp paredit bug where it can't indent a range if selected from the very beginning of the file
    start = range.start
    if start.column == 0 && start.row == 0
      start.column = 1

    editor.setSelectedScreenRange(range)
    atom.commands.dispatch(atom.views.getView(editor), 'lisp-paredit:indent')
```

`config.cson`:

```
"*":
  "bracket-matcher":
    autocompleteCharacters: [
      "()"
      "[]"
      "{}"
      "\"\""
      "“”"
      "‘’"
      "«»"
      "‹›"
    ]
  core:
    packagesWithKeymapsDisabled: [
      "lisp-paredit"
    ]
    telemetryConsent: "no"
  editor:
    autoIndentOnPaste: false
    scrollPastEnd: true
  "exception-reporting":
    userId: "043e4b48-e6ea-4f34-884d-356eb4d9154c"
  "lisp-paredit":
    enabled: false
    indentationForms: [
      "try"
      "catch"
      "finally"
      "/^let/"
      "are"
      "/^def/"
      "fn"
      "cond"
      "condp"
      "/^if.*/"
      "/.*\\/for/"
      "for"
      "for-all"
      "/^when.*/"
      "testing"
      "doseq"
      "dotimes"
      "ns"
      "routes"
      "GET"
      "POST"
      "PUT"
      "DELETE"
      "extend-protocol"
      "loop"
      "do"
      "case"
      "with-bindings"
      "checking"
      "with-open"
    ]
    strict: false
  minimap:
    plugins:
      "find-and-replace": true
      "find-and-replaceDecorationsZIndex": 0
      "highlight-selected": true
      "highlight-selectedDecorationsZIndex": 0
  "proto-repl":
    autoPrettyPrint: true
    bootPath: "/usr/local/bin/boot"
    leinPath: "/usr/local/bin/lein"
".clojure.source":
  editor:
    autoIndent: false
    autoIndentOnPaste: false
    nonWordCharacters: "()\"':,;~@#$%^&{}[]`"
    scrollPastEnd: true
    tabLength: 1
```

`keymap.cson`:

```
# Your keymap
#
# Atom keymaps work similarly to style sheets. Just as style sheets use
# selectors to apply styles to elements, Atom keymaps use selectors to associate
# keystrokes with events in specific contexts. Unlike style sheets however,
# each selector can only be declared once.
#
# You can create a new keybinding in this file by typing "key" and then hitting
# tab.
#
# Here's an example taken from Atom's built-in keymap:
#
# 'atom-text-editor':
#   'enter': 'editor:newline'
#
# 'atom-workspace':
#   'ctrl-shift-p': 'core:move-up'
#   'ctrl-p': 'core:move-down'
#
# You can find more information about keymaps in these guides:
# * http://flight-manual.atom.io/using-atom/sections/basic-customization/#_customizing_keybindings
# * http://flight-manual.atom.io/behind-atom/sections/keymaps-in-depth/
#
# If you're having trouble with your keybindings not working, try the
# Keybinding Resolver: `Cmd+.` on macOS and `Ctrl+.` on other platforms. See the
# Debugging Guide for more information:
# * http://flight-manual.atom.io/hacking-atom/sections/debugging/#check-the-keybindings
#
# This file uses CoffeeScript Object Notation (CSON).
# If you are unfamiliar with CSON, you can read more about it in the
# Atom Flight Manual:
# http://flight-manual.atom.io/using-atom/sections/basic-customization/#_cson

# Allows using enter to select an autocomplete suggestion.
'.platform-darwin atom-text-editor[data-grammar~="clojure"].autocomplete-active':
  'enter':          'autocomplete-plus:confirm'

'.platform-darwin atom-text-editor[data-grammar~="clojure"]':

  # Indent the current selection
  'cmd-i':          'lisp-paredit:indent'

  # Expand the selection up a block
  'ctrl-shift-m':   'lisp-paredit:expand-selection'

  # Provides proper indentation when enter is pressed. (I disable normal lisp-paredit keybindings)
  'enter':          'lisp-paredit:newline'

  # Helpers for cutting, copying, pasting, deleting, and indenting a Clojure code
  'cmd-shift-x':    'jason:cut-sexp'
  'cmd-shift-c':    'jason:copy-sexp'
  'cmd-shift-v':    'jason:paste-sexp'
  'cmd-shift-delete': 'jason:delete-sexp'
  'cmd-shift-d':    'jason:delete-sexp'
  'cmd-shift-i':    'jason:indent-top-sexp'


# Miscellaneous helpers. Less applicable to clojure code. (optional)
'.platform-darwin atom-workspace atom-text-editor':
  'alt-up': 'editor:move-line-up'
  'alt-down': 'editor:move-line-down'
  'cmd-alt-down': 'editor:duplicate-lines'
  'cmd-d': 'editor:delete-line'
  'ctrl-s':'tree-view:toggle'
  'cmd-e': 'find-and-replace:select-next'
  'cmd-alt-ctrl-w': 'editor:toggle-soft-wrap'
  'alt-backspace': 'editor:delete-to-previous-word-boundary'
  'alt-delete': 'editor:delete-to-next-word-boundary'
  'ctrl-d': 'proto-repl:exit-repl'
  'cmd-l':  'go-to-line:toggle'
  'ctrl-l': 'proto-repl:clear-repl'
```
`styles.less`:

```
/*
 * Your Stylesheet
 *
 * This stylesheet is loaded when Atom starts up and is reloaded automatically
 * when it is changed and saved.
 *
 * Add your own CSS or Less to fully customize Atom.
 * If you are unfamiliar with Less, you can read more about it here:
 * http://lesscss.org
 */


/*
 * Examples
 * (To see them, uncomment and save)
 */

// style the background color of the tree view
.tree-view {
  // background-color: whitesmoke;
}

// style the background and foreground colors on the atom-text-editor-element itself
atom-text-editor {
  // color: white;
  // background-color: hsl(180, 24%, 12%);
}

// style UI elements inside atom-text-editor
atom-text-editor .cursor {
  // border-color: red;
}

.proto-repl-repl::shadow .lisp-syntax-error .region {
  background-color: rgba(0, 0, 0, 0) !important;
}

```

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

## Project notes

With all weights activated some strange predictions were made. For instance:
the best weight curve fit for :WOL in GER resulted was increasing descend. That's
not strange if you look at the data: 15 5 1 8 15 8 11 5 2 8 16. Predicting the '16'
would be best if the first value '15' would have a lot of weight. But then the
new prediction would add a lot of weight to the '5' result, which probably results
in a bad prediction.

There are 2 things going on here:

1. I cannot logically explain what would be going on with a team that has a
   increasing descend
2. I probably need to get rid of the weights lookup table and replace it with
   the (bezier) formula they were derived from. Then the first '15' value would
   also be used in the new-position-prediction (then again: would 'what
   happened 11 years ago' really have this kind of weight? I don't think so.)

The frequencies of best-predictors with all weights on:

[:ia1 35] [:id1 10] [:v1 7] [:id2 4] [:dd1 3] [:ia3 2] [:da4 2] [:h1 2] [:la1 2] ...

The underlying data:

(sort-by val >
  (frequencies
    (mapcat
      vals
      [{:EXC :v1, :UTR :ia1, :ROD :ia1, :WIL :v1, :AZA :dd1, :HEA :ia1, :HEE :ia1,
        :PSV :h1, :VIT :ia1, :GRO :da4, :NAC :ia1, :SPR :id2, :ADO :ia1, :TWE :v1,
        :PEC :da3, :VVV :v3, :FEY :ia1, :AJA :id2}
       {:HSV :ia1, :BAY :ia1, :TSG :id1, :LEP :ia1, :LEV :id1, :DOR :ia1, :STU :ia1,
        :MON :da1, :WER :v1, :WOL :id1, :HAN :ia1, :MAI :id4, :SGE :dd1, :AUG :la1,
        :BER :v1, :SCH :da2, :FRE :da4, :KOE :ia1}
       {:SWA :v2 :BRI :ia1, :STK :h1, :BUR :ia1, :MCI :la1, :WHU :v1, :LIV :id2,
        :NEW :ia1, :ARS :id1, :TOT :ia1, :CRY :ia1, :HUD :dd1, :SOU :ia1,
        :EVE :h3, :LEI :ia1, :BOU :ia1, :WAT :ia1, :WBA :ia1, :CHE :id1,
        :MUN :ia1}
       {:VIL :v1, :ALV :ia1, :EIB :ia1, :SOC :ia1, :BET :ia2, :VAL :ia1,
        :ESY :id1, :CLV :ia3, :LAP :ia1, :GET :ia1, :SEV :id1, :REA :id1,
        :LEG :ia1, :COR :la2, :LVT :id2, :MLA :id1, :ATM :ia1, :GIR :ia1,
        :FCB :id1, :ATB :ia3}])))

The high number of :ia1 curves as best predictor makes sense.
Results of the last couple of years are *the* most important predictors.
Looking into the high number of :id1 cases, I see a pattern: these are mostly
teams with a 'strange' position last year compared to the years before (like :WOL).
But since I cannot logically explain what would be going on with a team that has
such a weight curve as logical predictor, I did another test.

I'm going to calculate with only 4 weight curves

- horizontal (the mean is the best predictor if you have absolutely no other info)
- increasing ascend (last couple of years are *the* most important predictors)
- linear ascend (same as prediction last year which wasn't too bad)
- decreasing ascend (team is performing different, but stable
  last years compared to the years before that.)

Then the frequencies turn into this:

([:ia1 42] [:hor 17] [:da1 9] [:la1 8])

The underlying data:

(sort-by val >
  (frequencies
    (mapcat
      vals
      [{:EXC :ia1, :UTR :ia1, :ROD :ia1, :WIL :ia1, :AZA :da1, :HEA :ia1,
        :HEE :ia1, :PSV :da1, :VIT :ia1, :GRO :la1, :NAC :ia1, :SPR :hor,
        :ADO :ia1, :TWE :la1, :PEC :da1, :VVV :da1, :FEY :ia1, :AJA :ia1}
       {:HSV :ia1, :BAY :ia1, :TSG :hor, :LEP :ia1, :LEV :hor, :DOR :ia1,
        :STU :ia1, :MON :da1, :WER :hor, :WOL :hor, :HAN :ia1, :MAI :hor,
        :SGE :hor, :AUG :la1, :BER :ia1, :SCH :da1, :FRE :la1, :KOE :ia1}
       {:SWA :da1, :BRI :ia1, :STK :da1, :BUR :ia1, :MCI :la1, :WHU :ia1,
        :LIV :hor, :NEW :ia1, :ARS :hor, :TOT :ia1, :CRY :ia1, :HUD :la1,
        :SOU :ia1, :EVE :hor, :LEI :ia1, :BOU :ia1, :WAT :ia1, :WBA :ia1,
        :CHE :hor, :MUN :ia1}
       {:VIL :ia1, :ALV :ia1, :EIB :ia1, :SOC :ia1, :BET :ia1, :VAL :ia1,
        :ESY :hor, :CLV :la1, :LAP :ia1, :GET :ia1, :SEV :hor, :REA :hor,
        :LEG :ia1, :COR :da1, :LVT :hor, :MLA :hor, :ATM :ia1, :GIR :ia1,
        :FCB :hor, :ATB :la1}])))

  On top of these 4 weight curves, I would love to plugin 1 more prediction:
  simple linear regression and compare it with these results.

## License

Use / change / redistribute at your own risk. It is not copyrighted. I'm standing on the shoulders of giants here.
