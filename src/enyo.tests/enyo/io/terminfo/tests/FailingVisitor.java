package enyo.io.terminfo.tests;

import enyo.io.terminfo.ScriptVisitor;

import enyo.testing.Testing;

abstract class FailingVisitor implements ScriptVisitor {
    final Testing t;

    FailingVisitor(Testing t) {
        this.t = t;
    }

    @Override
    public void visit(UnaryOp op) {
        t.assertFail("visit(UnaryOp) called");
    }

    @Override
    public void visit(BinaryOp op) {
        t.assertFail("visit(BinaryOp) called");
    }

    @Override
    public void visit(Format op) {
        t.assertFail("visit(Format) called");
    }

    @Override
    public void visitIncrement() {
        t.assertFail("visitIncrement() called");
    }

    @Override
    public void visitPushParameter(int index) {
        t.assertFail("visitPushParameter(int) called");
    }

    @Override
    public void visitPushVariable(int var) {
        t.assertFail("visitPushVariable(int) called");
    }

    @Override
    public void visitSetVariable(int var) {
        t.assertFail("visitSetVariable(int) called");
    }

    @Override
    public void visitPushConstant(int value) {
        t.assertFail("visitPushConstant(int) called");
    }

    @Override
    public void visit(byte value) {
        t.assertFail("visitIf(byte) called");
    }

    @Override
    public IfVisitor visitIf() {
        t.assertFail("visitIf() called");
        return null; // Not reached.
    }
}
