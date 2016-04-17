public class Token {

    public int fila;
    public int columna;

    public String lexema;
    public String etiqueta;

    public int tipo; 	// tipo es: ID, ENTERO, REAL ...
    
    //public static final String[] etiquetas = 
   // {"(", ")", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", 

    public static final int 
            ERROR           = -2,
            INIT            = -1,
            PARI            = 0,
            PARD            = 1,
            MULOP           = 2,  
            ADDOP           = 3,
            PYC             = 4,
            COMA            = 5,
            ASIG            = 6,
            LLAVEI          = 7,
            LLAVED          = 8,
            CORI            = 9,
            CORD            = 10,
            DOUBLE          = 11,
            INT             = 12,
            MAIN            = 13,
            ENTERO          = 14,
            ID              = 15,
            REAL            = 16,
            ENTERO1         = 17,
            ENTERO2         = 18,      
            MULOP1          = 19,
            MULOP2          = 20,
            NUMBER          = 21, // Despues de recibir un numero
            IDAUX           = 22, // Despues de recibir una letra
            COMMENTorMULOP  = 23, // /
            OPENCOMMENT     = 24, // /*
            CLOSECOMMENT    = 25, // Despues de abrir comentario y encontrar asterisco (faltaria / para cerrar y volver a 0
            REALAUX1        = 26, // Despues de recibir un punto
            REALAUX2        = 27, // Despues de recibir punto + nro
            EOF             = 28;

    public Token() {
        lexema = new String();
        etiqueta = new String();
    }

    public String toString() {
        return etiqueta;
    }
    
    public void setTipo(int status)
    {
        if(status == MULOP1 || status == MULOP2)
            tipo = MULOP;
        else if(status == ENTERO1 || status == ENTERO2)
            tipo = ENTERO;
        else
            tipo = status;
    }
    
    public static String getLabel(int status)
    {
        switch(status)
        {
            case PARI:
                return "(";
            case PARD:
                return ")";
            case MULOP:
                return "*/";
            case ADDOP:
                return "+-";
            case PYC:
                return ";";
            case COMA:
                return ",";
            case ASIG:
                return "=";
            case LLAVEI:
                return "{";
            case LLAVED:
                return "}";
            case CORI:
                return "[";
            case CORD:
                return "]";
            case DOUBLE:
                return "'double'";
            case INT:
                return "'int'";
            case MAIN:
                return "'main'";
            case ENTERO:
                return "numero entero";
            case ID:
                return "identificador";
            case REAL:
                return "numero real";
            case EOF:
                return "fin de fichero";
        }
        return "ERROR LABEL";
    }
}