// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.codeInsight.template.emmet;

import com.intellij.codeInsight.template.emmet.tokens.*;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class EmmetLexer {
  private static final String DELIMS = ">^+*|()[]{}.#,='\" \0";

  public @Nullable List<ZenCodingToken> lex(@NotNull String text) {
    text += ZenCodingTemplate.MARKER;
    final List<ZenCodingToken> result = new ArrayList<>();

    boolean inQuotes = false;
    boolean inApostrophes = false;
    int bracesStack = 0;

    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < text.length(); i++) {
      final char c = text.charAt(i);

      if (inQuotes) {
        builder.append(c);
        if (c == '"') {
          inQuotes = false;
          result.add(new StringLiteralToken(builder.toString()));
          builder = new StringBuilder();
        }
        continue;
      }

      if (inApostrophes) {
        builder.append(c);
        if (c == '\'') {
          inApostrophes = false;
          result.add(new StringLiteralToken(builder.toString()));
          builder = new StringBuilder();
        }
        continue;
      }

      if (bracesStack > 0) {
        builder.append(c);
        if (c == '}') {
          bracesStack--;
          if (bracesStack == 0) {
            result.add(new TextToken(builder.toString()));
            builder = new StringBuilder();
          }
        }
        else if (c == '{') {
          bracesStack++;
        }
        continue;
      }

      if (DELIMS.indexOf(c) < 0) {
        builder.append(c);
      }
      else {
        // handle special case: ul+ template
        if (c == '+' && (i == text.length() - 2 || text.charAt(i + 1) == ')')) {
          builder.append(c);
          continue;
        }

        if (!builder.isEmpty()) {
          final String tokenText = builder.toString();
          final int n = StringUtil.parseInt(tokenText, -1);
          if (!StringUtil.startsWithChar(tokenText, '0') && n >= 0) {
            result.add(new NumberToken(n));
          }
          else {
            result.add(new IdentifierToken(tokenText));
          }
          builder = new StringBuilder();
        }
        if (c == '"') {
          inQuotes = true;
          builder.append(c);
        }
        else if (c == '\'') {
          inApostrophes = true;
          builder.append(c);
        }
        else if (c == '{') {
          bracesStack = 1;
          builder.append(c);
        }
        else if (c == '(') {
          result.add(ZenCodingTokens.OPENING_R_BRACKET);
        }
        else if (c == ')') {
          result.add(ZenCodingTokens.CLOSING_R_BRACKET);
        }
        else if (c == '[') {
          result.add(ZenCodingTokens.OPENING_SQ_BRACKET);
        }
        else if (c == ']') {
          result.add(ZenCodingTokens.CLOSING_SQ_BRACKET);
        }
        else if (c == '=') {
          result.add(ZenCodingTokens.EQ);
        }
        else if (c == '.') {
          result.add(ZenCodingTokens.DOT);
        }
        else if (c == '#') {
          result.add(ZenCodingTokens.SHARP);
        }
        else if (c == ',') {
          result.add(ZenCodingTokens.COMMA);
        }
        else if (c == ' ') {
          result.add(ZenCodingTokens.SPACE);
        }
        else if (c == '|') {
          result.add(ZenCodingTokens.PIPE);
        }
        else if (c != ZenCodingTemplate.MARKER) {
          result.add(new OperationToken(c));
        }
      }
    }
    if (bracesStack != 0 || inQuotes || inApostrophes) {
      return null;
    }
    return result;
  }
}
