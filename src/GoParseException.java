public class GoParseException extends Exception{
    public GoParseException(String message) {
        super("Parse error:\n"+message);
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }
}
