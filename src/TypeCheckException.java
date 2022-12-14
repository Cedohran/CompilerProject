public class TypeCheckException extends Exception{
    public TypeCheckException(String message) {
        super("Typecheck error:\n"+message);
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }
}
