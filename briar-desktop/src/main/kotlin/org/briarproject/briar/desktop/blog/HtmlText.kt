/*
 * Briar Desktop
 * Copyright (C) 2023 The Briar Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.briarproject.briar.desktop.blog

import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import org.briarproject.briar.desktop.blog.ListType.ORDERED
import org.briarproject.briar.desktop.blog.ListType.UNORDERED
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import org.jsoup.select.NodeVisitor

// This file is adapted from https://github.com/jeremyrempel/yahnapp (HtmlText.kt)

enum class ListType {
    ORDERED,
    UNORDERED
}

val listBullets = listOf(
    "\u2022",
    "\u25e6",
    "\u25aa",
)

@Composable
@Suppress("HardCodedStringLiteral")
fun HtmlText(
    html: String,
    modifier: Modifier = Modifier,
    overflow: TextOverflow = TextOverflow.Clip,
    maxLines: Int = Int.MAX_VALUE,
    handleLink: (String) -> Unit,
) {

    data class IndentInfo(val indent: TextUnit, val start: Int)

    val indentStack = ArrayDeque<IndentInfo>()
    val listNesting = ArrayDeque<ListType>()
    val listNumbering = ArrayDeque<Int>()

    var lastCharWasNewline = true
    var withinPre = false

    // Elements we support:
    //    "h1", "h2", "h3", "h4", "h5", "h6",
    //    "a", "b"/"strong", "i"/"em"/"cite", "u", "strike", "sub", "sup", "q", "small"
    //    "ul", "ol", "li"
    //    "br", "p", "blockquote", "pre",

    // Elements from Jsoup's safelist we could still add support for:
    //    "dd", "dl", "dt", "span"

    val h1 = MaterialTheme.typography.h1
    val h2 = MaterialTheme.typography.h2
    val h3 = MaterialTheme.typography.h3
    val h4 = MaterialTheme.typography.h4
    val h5 = MaterialTheme.typography.h5
    val h6 = MaterialTheme.typography.h6
    val bold = SpanStyle(fontWeight = FontWeight.Bold)
    val italic = SpanStyle(fontStyle = FontStyle.Italic)
    val underline = SpanStyle(textDecoration = TextDecoration.Underline)
    val strikethrough = SpanStyle(textDecoration = TextDecoration.LineThrough)
    // todo: combination underline + strikethrough not working
    val subscript = SpanStyle(baselineShift = BaselineShift.Subscript)
    val superscript = SpanStyle(baselineShift = BaselineShift.Superscript)
    val small = SpanStyle(fontSize = 0.8.em)
    val link = SpanStyle(textDecoration = TextDecoration.Underline, color = MaterialTheme.colors.primaryVariant)
    val monospace = SpanStyle(fontFamily = FontFamily.Monospace) // todo: doesn't work for some reason

    val formattedString = remember(html) {
        buildAnnotatedString {
            var cursorPosition = 0 // todo: couldn't this be replaced with this.length?
            fun appendAndUpdateCursor(str: String) {
                append(str)
                cursorPosition += str.length
                lastCharWasNewline = str.last() == '\n'
            }

            fun ensureNewline() {
                if (!lastCharWasNewline) appendAndUpdateCursor("\n")
            }

            fun pushIndent(indent: TextUnit) {
                check(indent.isSp) { "only TextUnit.sp allowed" }

                var combinedIndent = indent
                if (indentStack.isNotEmpty()) {
                    val prev = indentStack.top()
                    if (prev.start < cursorPosition) {
                        println("pushIndent: ${prev.start}-$cursorPosition")
                        addStyle(
                            style = ParagraphStyle(textIndent = TextIndent(prev.indent, prev.indent)),
                            start = prev.start,
                            end = cursorPosition
                        )
                        // ensureNewline()
                    }
                    combinedIndent = (prev.indent.value + indent.value).sp
                }

                indentStack.push(IndentInfo(combinedIndent, cursorPosition))
            }

            fun popIndent() {
                check(indentStack.isNotEmpty()) { "nothing to pop from" }
                val prev = indentStack.pop()
                if (prev.start < cursorPosition) {
                    println("popIndent: ${prev.start}-$cursorPosition")
                    addStyle(
                        style = ParagraphStyle(textIndent = TextIndent(prev.indent, prev.indent)),
                        start = prev.start,
                        end = cursorPosition
                    )
                    ensureNewline()
                }

                if (indentStack.isNotEmpty()) {
                    val next = indentStack.pop()
                    indentStack.push(next.copy(start = cursorPosition))
                }
            }

            data class HtmlNode(
                val start: (node: Element) -> Unit,
                val end: () -> Unit = { pop() },
            )

            val strong = HtmlNode(start = { pushStyle(bold) })
            val em = HtmlNode(start = { pushStyle(italic) })
            fun pushHeader(style: TextStyle) {
                pushStyle(style.toParagraphStyle())
                pushStyle(style.toSpanStyle())
            }

            fun popHeader() {
                pop(); pop()
            }

            val nodes = mapOf(
                // headers
                "h1" to HtmlNode(start = { pushHeader(h1) }, end = ::popHeader),
                "h2" to HtmlNode(start = { pushHeader(h2) }, end = ::popHeader),
                "h3" to HtmlNode(start = { pushHeader(h3) }, end = ::popHeader),
                "h4" to HtmlNode(start = { pushHeader(h4) }, end = ::popHeader),
                "h5" to HtmlNode(start = { pushHeader(h5) }, end = ::popHeader),
                "h6" to HtmlNode(start = { pushHeader(h6) }, end = ::popHeader),

                // inline formatting
                "b" to strong, "strong" to strong,
                "i" to em, "em" to em, "cite" to em,
                "u" to HtmlNode(start = { pushStyle(underline) }),
                "strike" to HtmlNode(start = { pushStyle(strikethrough) }),
                "sub" to HtmlNode(start = { pushStyle(subscript) }),
                "sup" to HtmlNode(start = { pushStyle(superscript) }),
                "small" to HtmlNode(start = { pushStyle(small) }),
                "code" to HtmlNode(
                    start = { if (!withinPre) pushStyle(monospace) },
                    end = { if (!withinPre) pop() }
                ),
                "q" to HtmlNode(
                    // todo: quotation marks should be properly localized
                    start = { appendAndUpdateCursor("\"") },
                    end = { appendAndUpdateCursor("\"") }
                ),
                "a" to HtmlNode(
                    start = { node ->
                        val href = node.attr("href")

                        pushStringAnnotation("link", href)
                        pushStyle(link)
                    },
                    end = {
                        pop()
                        pop()
                    }
                ),

                // lists
                "ul" to HtmlNode(
                    start = {
                        listNesting.push(UNORDERED)
                        listNumbering.push(0)
                        pushIndent(20.sp)
                    },
                    end = {
                        listNesting.pop()
                        listNumbering.pop()
                        popIndent()
                    }
                ),
                "ol" to HtmlNode(
                    start = {
                        listNesting.push(ORDERED)
                        listNumbering.push(0)
                        pushIndent(20.sp)
                    },
                    end = {
                        listNesting.pop()
                        listNumbering.pop()
                        popIndent()
                    }
                ),
                "li" to HtmlNode(
                    start = {
                        check(listNesting.isNotEmpty()) { "<li> outside of list" }
                        val listType = listNesting.top()
                        listNumbering.incrementCurrent()
                        if (listType == UNORDERED) {
                            val bulletType = listNesting.size - 1
                            appendAndUpdateCursor(listBullets[bulletType % listBullets.size])
                            appendAndUpdateCursor(" ")
                            pushStringAnnotation("bullet", listNesting.size.toString())
                        } else if (listType == ORDERED) {
                            appendAndUpdateCursor("${listNumbering.top()}. ")
                            pushStringAnnotation("bullet", listNesting.size.toString())
                        }
                    },
                    end = {
                        pop()
                        ensureNewline()
                    }
                ),

                // misc
                "br" to HtmlNode(start = { appendAndUpdateCursor("\n") }, end = {}),
                "p" to HtmlNode(
                    start = { pushIndent(0.sp) },
                    end = { popIndent() }
                ),
                "blockquote" to HtmlNode(
                    start = {
                        pushIndent(20.sp)
                        pushStringAnnotation("quote", "")
                    },
                    end = {
                        pop()
                        popIndent()
                    }
                ),
                "pre" to HtmlNode(
                    start = {
                        pushIndent(20.sp)
                        withinPre = true
                        pushStyle(monospace)
                    },
                    end = {
                        popIndent()
                        withinPre = false
                        pop()
                    }
                )
            )

            val doc = Jsoup.parse(html)

            doc.traverse(object : NodeVisitor {
                override fun head(node: Node, depth: Int) {
                    when (node) {
                        is TextNode -> {
                            val text =
                                if (withinPre) node.wholeText
                                else node.text()
                                    // replace multiple newlines/whitespaces to single whitespace
                                    .replace("\\s+".toRegex(), " ")
                                    // remove whitespace if first character in new line
                                    .let { if (lastCharWasNewline) it.trimStart() else it }
                            if (text.isNotBlank()) {
                                appendAndUpdateCursor(text)
                            }
                        }

                        is Element -> {
                            nodes[node.tagName()]?.let { it.start(node) }
                        }

                        else -> {
                            throw Exception("Unknown node type")
                        }
                    }
                }

                override fun tail(node: Node, depth: Int) {
                    if (node is Element) {
                        nodes[node.tagName()]?.let { it.end() }
                    }
                }
            })
        }
    }

    var onDraw: DrawScope.() -> Unit by remember { mutableStateOf({}) }

    ClickableText(
        text = formattedString,
        modifier = modifier.drawBehind { onDraw() },
        overflow = overflow,
        maxLines = maxLines,
        style = MaterialTheme.typography.body1.copy(color = MaterialTheme.colors.onSurface),
        onClick = { offset ->
            formattedString
                .getStringAnnotations(start = offset, end = offset)
                .firstOrNull { it.tag == "link" }
                ?.let { annotation ->
                    handleLink(annotation.item)
                }
        },
        onTextLayout = { layoutResult ->
            val quotes = formattedString.getStringAnnotations("quote").map {
                val firstLine = layoutResult.getLineForOffset(it.start)
                val lastLine = layoutResult.getLineForOffset(it.end - 1)
                Rect(
                    top = layoutResult.getLineTop(firstLine),
                    bottom = layoutResult.getLineBottom(lastLine),
                    left = 0f,
                    right = 0f
                )
            }

            onDraw = {
                quotes.forEach {
                    val line = it.copy(left = it.left + 10.dp.toPx(), right = it.right + 11.dp.toPx())
                    drawRect(
                        color = Color.Green,
                        topLeft = line.topLeft,
                        size = line.size,
                    )
                }
            }
        }
    )
}

private fun AnnotatedString.getStringAnnotations(tag: String) =
    getStringAnnotations(tag, 0, length)

fun <E> ArrayDeque<E>.push(e: E) {
    addLast(e)
}

fun <E> ArrayDeque<E>.pop(): E {
    return removeLast()
}

fun <E> ArrayDeque<E>.top(): E {
    return last()
}

fun ArrayDeque<Int>.incrementCurrent() {
    this[size - 1] = this[size - 1] + 1
}
