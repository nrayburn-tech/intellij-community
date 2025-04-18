// "Make '<set-attribute>' public" "true"
// PRIORITY: HIGH
// ACTION: Converts the assignment statement to an expression
// ACTION: Make '<set-attribute>' internal
// ACTION: Make '<set-attribute>' public

class Demo {
    var attribute = "a"
        private set
}

fun main() {
    val c = Demo()
    <caret>c.attribute = "test"
}
// FUS_QUICKFIX_NAME: org.jetbrains.kotlin.idea.quickfix.ChangeVisibilityFix$ChangeToPublicFix
// FUS_K2_QUICKFIX_NAME: org.jetbrains.kotlin.idea.k2.codeinsight.fixes.ChangeVisibilityFixFactories$ChangeToPublicModCommandAction