# Regexcv
Regexcv is a command line tool that is used to generate a DFA/NFA digraph denoted by the [Graphviz Dot Language](http://graphviz.org/) from a regular expression.

# Feature

1. Generate NFA/DFA digraph.
2. Minimize generated DFA.
3. Remove `ε-closure` of NFA.

# Getting Started
Regexcv is wrote in Java. If you want to run this tool on your computer, the JRE(1.8 or more) is required.

Download jar:
``` shell
wget https://github.com/nano-go/regexcv/releases/download/1.0.0/Regexcv-1.0.0.jar
```

Run:
``` shell
java -jar Regexcv-1.0.0.jar -v
```

Or use alias to run:
``` shell
alias regexcv='java -jar regexcv_jar_path'

regexcv --help
```

# Regular Expression
Regexcv supports only the simple regular expression syntax.

## 1. Char Classes

The regex `[A-Za-z0-9_$]` can match the following strings:
```
'a', 'b', 'C', '_', '$'...
```

But the nagated character class syntax is not supported:  

The regex `[^abc]` only match the following strings:
```
'a', 'b', 'c', '^'
```

The nagate character `^` will be recognized as a normal single character.

## 2. Choices

Uses `|` to choice multiple regular expressions.


The regex `a|b|c` only matches the following strings:
```
'a', 'b', 'c'
```

## 3. Special Characters

In regexcv, uses the `.`, `\w`, `\d`, `\s` special characters to represent `any char`, `[a-zA-Z0-9]`, `[0-9]`, `spacewhite`.


The regex `.\w\d\s` can match the following strings:
```
'%a0 ', '_A9 ', '$A8\t'...
```

## 4. Quantifiers

1. Zero or more: `*`
2. One or more: `+`
3. Optional: `?`

## 5. Parentheses
You can use parenthese to denote priority in the order of regular expressions.

The regex `(abc)*` can match the following strings:
```
'', 'abc', 'abcabc'...
```

## 6. Unicode
Regexcv supports unicode characters.

The regex `(你好)|(Hello)` matches the following strings:
```
'你好', 'Hello'
```

## 7. Escape Characters
You can use `\` to escape meta-characters in the regular expression.

For example: The regex `\(\)\[\]` matches the string `()[]`.

# Examples

##  NFA

Regex `(abc)*`:
``` shell
regexcv -N '(abc)*'
```

Ues Graphviz Dot to generate the following image:

!['(abc)*' NFA](https://raw.githubusercontent.com/nano-go/regexcv/main/resources/nfa1.png)

Regex `<\w+>.*<\w+>`:
``` shell
regexcv -N -e -r '<\w+>.*<\w+>'
```

The generated image:

!['<\w+>.*<\w+>' NFA](https://raw.githubusercontent.com/nano-go/regexcv/main/resources/nfa2.png)

## DFA

Regex `(a|b)*abb`:
``` shell
regexcv -D '(a|b)*abb'
```

The generated image:

!['(a|b)*abb' DFA](https://raw.githubusercontent.com/nano-go/regexcv/main/resources/dfa1.png)


Regex `(from)|(frog)`:
``` shell
regexcv -D -m '(from)|(frog)'
```

The generated image:

!['(from)|(frog)' DFA](https://raw.githubusercontent.com/nano-go/regexcv/main/resources/dfa2.png)
