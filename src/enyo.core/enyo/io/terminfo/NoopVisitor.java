package enyo.io.terminfo;

class NoopVisitor implements ScriptVisitor {
    @Override
    public void visit(UnaryOp op) {
    }

    @Override
    public void visit(BinaryOp op) {
    }

    @Override
    public void visit(Format op) {
    }

    @Override
    public void visitIncrement() {
    }

    @Override
    public void visitPushParameter(int var) {
    }

    @Override
    public void visitPushVariable(int var) {
    }

    @Override
    public void visitSetVariable(int var) {
    }

    @Override
    public void visitPushConstant(int var) {
    }

    @Override
    public void visit(byte value) {
    }

    @Override
    public IfVisitor visitIf() {
        return null;
    }

    private NoopVisitor() {
    }

    static final ScriptVisitor INSTANCE = new NoopVisitor();
}
