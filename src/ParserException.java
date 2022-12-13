public class ParserException extends Exception{
    public ParserException(String message) {
        super("Parser error: "+message);
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }
}
