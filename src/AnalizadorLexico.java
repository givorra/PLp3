import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.LinkedList;
import java.util.Queue;

public class AnalizadorLexico {

    private RandomAccessFile file;
    private Queue<Character> buffer;
    private int fila;
    private int columna;

    public AnalizadorLexico(RandomAccessFile file) {
        this.file = file;
        fila = 1;
        columna = 0;
        buffer = new LinkedList<Character>();
    }

    public Token siguienteToken() {
        Token token = new Token();
        char c;
        int status = Token.INIT;

        do 
        {
            c = leerCaracter();
            ++columna;

            if (c == '\n') // Se ignoran los saltos de linea, pero se tiene en cuenta para fila y columna
            {
                ++fila;
                columna = 0;
            } 
            //else if(c == Token.EOF && status != Token.OPENCOMMENT && status != Token.CLOSECOMMENT) // Caso especial, si llega EOF en mitad de un comentario
            else if(c == Token.EOF && status == Token.INIT)
            {
                token.tipo = Token.EOF;
                token.etiqueta = "fin de fichero";
                return token;
            }
            else
            {
                int newStatus = delta(status, c, token);

                if (newStatus == Token.ERROR) {
                    errorLexico(c);
                }

                if (isFinal(newStatus)) {
                    token.setTipo(newStatus);
                    return token;
                } else {
                    status = newStatus;
                }
            }
        } while (true);

        //return token;
    }
/*
    Esta funcion recibe el estado actual, el último caracter leido y el token actual.
    En base al estado recibido, hace las comprobaciones correspondientes para ver que
    transición es la que corresponde para determinar el siguiente estado.
    También va concatenando de forma adecuada el caracter recibido al token
*/
    private int delta(int status, char c, Token token) {
        switch (status) {
            case Token.INIT: {
                switch (c) {
                    case ' ': case '\t':
                        return Token.INIT;
                    case '(':
                        token.etiqueta = "(";
                        token.lexema = token.etiqueta;
                        token.fila = fila;
                        token.columna = columna;
                        return Token.PARI;
                    case ')':
                        token.etiqueta = ")";
                        token.lexema = token.etiqueta;
                        token.fila = fila;
                        token.columna = columna;
                        return Token.PARD;
                    case '*':
                        token.etiqueta = "*/";
                        token.lexema += c;
                        token.fila = fila;
                        token.columna = columna;
                        return Token.MULOP1;
                    case '+':case '-':
                        token.etiqueta = "+-";
                        token.lexema += c;
                        token.fila = fila;
                        token.columna = columna;
                        return Token.ADDOP;
                    case ';':
                        token.etiqueta = ";";
                        token.lexema = token.etiqueta;
                        token.fila = fila;
                        token.columna = columna;
                        return Token.PYC;
                    case ',':
                        token.etiqueta = ",";
                        token.lexema = token.etiqueta;
                        token.fila = fila;
                        token.columna = columna;
                        return Token.COMA;
                    case '=':
                        token.etiqueta = "=";
                        token.lexema = token.etiqueta;
                        token.fila = fila;
                        token.columna = columna;
                        return Token.ASIG;
                    case '{':
                        token.etiqueta = "{";
                        token.lexema = token.etiqueta;
                        token.fila = fila;
                        token.columna = columna;
                        return Token.LLAVEI;
                    case '}':
                        token.etiqueta = "}";
                        token.lexema = token.etiqueta;
                        token.fila = fila;
                        token.columna = columna;
                        return Token.LLAVED;
                    case '[':
                        token.etiqueta = "[";
                        token.lexema = token.etiqueta;
                        token.fila = fila;
                        token.columna = columna;
                        return Token.CORI;
                    case ']':
                        token.etiqueta = "]";
                        token.lexema = token.etiqueta;
                        token.fila = fila;
                        token.columna = columna;
                        return Token.CORD;
                    case '0':case '1':case '2':case '3':case '4':case '5':case '6':case '7':case '8':case '9':
                        token.fila = fila;
                        token.columna = columna;
                        token.lexema += c;
                        return Token.NUMBER;
                    case '/':
                        token.fila = fila;
                        token.columna = columna;
                        token.lexema += c;
                        return Token.COMMENTorMULOP;
                    default:
                        token.fila = fila;
                        token.columna = columna;
                        if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
                            token.lexema += c;
                            return Token.IDAUX;
                        } else {
                            return Token.ERROR;
                        }
                }
            }
            case Token.NUMBER: {
                if (c >= '0' && c <= '9') 
                {
                    token.lexema += c;
                    return Token.NUMBER;
                } else if (c == '.') {
                    //buffer.add(c);
                    token.lexema += c;
                    return Token.REALAUX1;
                } else {
                    buffer.add(c);
                    --columna;
                    token.etiqueta = "numero entero";
                    return Token.ENTERO1;
                }
            }
            case Token.REALAUX1: {
                if (c >= '0' && c <= '9') {
                    buffer.clear();
                    token.lexema += c;
                    return Token.REALAUX2;
                } else {
                    token.lexema = token.lexema.substring(0, token.lexema.length()-1);
                    buffer.add('.');
                    buffer.add(c);
                    columna -= 2;
                    token.etiqueta = "numero entero";
                    return Token.ENTERO2;
                }
            }
            case Token.REALAUX2: {
                if (c >= '0' && c <= '9') {
                    token.lexema += c;
                    return Token.REALAUX2;
                } else {
                    buffer.add(c);
                    --columna;
                    token.etiqueta = "numero real";
                    return Token.REAL;
                }
            }
            case Token.IDAUX: {
                if ((c >= '0' && c <= '9') || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
                    token.lexema += c;
                    return Token.IDAUX;
                } else {
                    buffer.add(c);
                    --columna;

                    switch (token.lexema) {
                        case "double":
                            token.etiqueta = "'" + token.lexema + "'";
                            return Token.DOUBLE;
                        case "int":
                            token.etiqueta = "'" + token.lexema + "'";
                            return Token.INT;
                        case "main":
                            token.etiqueta = "'" + token.lexema + "'";
                            return Token.MAIN;
                        default:
                            token.etiqueta = "identificador";
                            return Token.ID;
                    }
                }
            }
            case Token.COMMENTorMULOP: {
                if (c == '*') {
                    token.lexema = "";
                    return Token.OPENCOMMENT;
                } else {
                    buffer.add(c);
                    --columna;
                    token.etiqueta = "*/";
                    return Token.MULOP2;
                }
            }
            case Token.OPENCOMMENT: {
                if (c == '*') {
                    return Token.CLOSECOMMENT;
                } else if (c == Token.EOF) {
                    errorComentario();
                } else {
                    return Token.OPENCOMMENT;
                }
            }
            case Token.CLOSECOMMENT: {
                if (c == '/') {
                    return Token.INIT;
                } else if (c == '*') {
                    return Token.CLOSECOMMENT;
                } else if (c == Token.EOF)
                    errorComentario();
                else {
                    return Token.OPENCOMMENT;
                } 
            }
        }

        return Token.ERROR;
    }

    private boolean isFinal(int status) {
        return (status >= Token.PARI && status <= Token.MULOP2);   // Tiene que ser coherente con los estados declarados en la clase Token
    }

    private char leerCaracter() {
        char currentChar;

        try {
            if (buffer.isEmpty()) {
                currentChar = (char) file.readByte();
            } else {
                currentChar = buffer.poll();
            }
            return currentChar;
        } catch (EOFException e) {
            return Token.EOF;		// constante estática de la clase
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

        return ' ';
    }

    private void errorLexico(char c) {
        System.err.println("Error lexico (" + fila + "," + columna + "): caracter '" + c + "' incorrecto");
        System.exit(-1);
    }

    private void errorComentario() {
        System.err.println("Error lexico: fin de fichero inesperado");
        System.exit(-1);
    }
}
