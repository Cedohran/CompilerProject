public class TypeCheckException extends Exception{
    public TypeCheckException(String message) {
        super("TypeCheck Error: "+message);
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }
}
