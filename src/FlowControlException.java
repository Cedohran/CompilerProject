public class FlowControlException extends Exception {
    public FlowControlException(String message) {
        super("Flow control error:\n"+message);
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }
}
