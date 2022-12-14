public class ParseException extends Exception{
    public ParseException(String message) {
        super("Parse error:\n"+message);
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }
}
