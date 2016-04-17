
import java.util.ArrayList;
import java.util.Objects;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author gacel
 */
public class TraductorDR {
    private class Atributos
    {
        public String trad;
        public String th;
        public String tipo;
        public String cuerpo;
        public String declaracion;
        public String prefijo;
        public boolean esvariable;
        
        public Atributos()
        {
            trad        = new String("");
            th          = new String("");
            tipo        = new String("");
            cuerpo      = new String("");
            declaracion = new String("");
            prefijo     = new String("");
            esvariable  = false;
        }
        public Atributos(String trad, String th, String tipo, String cuerpo, String declaracion, String prefijo)
        {
            this.trad        = trad;
            this.th          = th;
            this.tipo        = tipo;
            this.cuerpo      = cuerpo;
            this.declaracion = declaracion;
            this.prefijo     = prefijo;
            this.esvariable  = false;
        }
    }
    private class Simbolo
    {
        public String nombre;
        public String tipo;
        public String valor;
        public boolean esArray;

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 53 * hash + Objects.hashCode(this.nombre);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Simbolo other = (Simbolo) obj;
            if (!Objects.equals(this.nombre, other.nombre)) {
                return false;
            }
            return true;
        }
        
        public Simbolo()
        {
            nombre = new String("");
            tipo    = new String("");
            valor   = new String("");
            esArray = false;
        }
        
        public Simbolo(String nombre, String tipo, String valor, boolean esArray)
        {
            this.nombre     = nombre;
            this.tipo       = tipo;
            this.valor      = valor;
            this.esArray    = esArray;
        }
    }
    private class Ambito
    {
        public String nombre;
        public ArrayList<Simbolo> simbolos;
        public Ambito padre;
        
        public Ambito()
        {
            nombre      = new String("");
            simbolos    = new ArrayList<Simbolo>();
            padre       = null;
        }
        public Ambito(String nombre, Ambito padre)
        {
            this.nombre = nombre;
            simbolos    = new ArrayList<Simbolo>();
            this.padre  = padre;
        }
        public Ambito(String nombre)
        {
            this.nombre = nombre;
            simbolos    = new ArrayList<Simbolo>();
            this.padre  = padre;
        }
    }

    public Token token;
    public AnalizadorLexico analizadorLexico;
    public StringBuilder reglas;
    public ArrayList<Ambito> ambitos;
    public boolean imprimeReglas;
    public static boolean debug = false;
    
    TraductorDR(AnalizadorLexico al)
    {
        analizadorLexico = al;
        token = al.siguienteToken();
        imprimeReglas = false;
    }
    
    private void init()
    {
        reglas = new StringBuilder();
        ambitos = new ArrayList<Ambito>();
    }
    
    public final void emparejar(int tokEsperado)
    {
        if (token.tipo == tokEsperado)
            token = analizadorLexico.siguienteToken();
        else
            errorSintaxis(tokEsperado);        
    }

    public void errorSintaxis(int... tokEsperado)
    {
        String s = new String();
        for(int t: tokEsperado)
        {
            s += " "+Token.getLabel(t);
        }
        if(token.tipo == Token.EOF)
            System.err.println("Error sintactico: encontrado fin de fichero, esperaba"+s);
        else
            System.err.println("Error sintactico ("+token.fila+","+token.columna+"): encontrado '"+token.lexema+"', esperaba"+s);
        System.exit(-1);
    }
    
    public void errorSemanticoIdRepetido()
    {
        System.err.println("Error semantico (" + token.fila + "," + token.columna + "): \'" + token.lexema + "\' ya existe en este ambito");
        System.exit(-1);
    }
    public void errorSemanticoArray()
    {
        System.err.println("Error semantico (" + token.fila + "," + token.columna + "): \'" + token.lexema + "\' debe ser mayor que cero");
        System.exit(-1);
    }
    public void errorSemanticoIdNoDeclarado()
    {
        System.err.println("Error semantico (" + token.fila + "," + token.columna + "): \'" + token.lexema + "\' no ha sido declarado");
        System.exit(-1);
    }
    public void errorSemanticoIdNoEsVariable()
    {
        System.err.println("Error semantico (" + token.fila + "," + token.columna + "): \'" + token.lexema + "\' no es una variable");
        System.exit(-1);
    }
    public void errorSemanticoTipoIncompatible()
    {
        System.err.println("Error semantico (" + token.fila + "," + token.columna + "): \'" + token.lexema + "\' debe ser de tipo real");
        System.exit(-1);
    }
    
    public void comprobarFinFichero()
    {
        if(token.tipo == Token.EOF && imprimeReglas)
            System.out.println(reglas);
    }
    
    public boolean existeSimbolo(String ambito, String simbolo)
    {
        Ambito a = getAmbito(ambito);
        while(true)
        {
            for(Simbolo s: a.simbolos)
            {
                if(s.nombre.equals(simbolo))
                    return true;
            }
            if(a.padre != null)
                a = a.padre;
            else
                return false;
        }
    }
    public Simbolo getSimbolo(String ambito, String simbolo)
    {
        Ambito a = getAmbito(ambito);
        while(true)
        {
            for(Simbolo s: a.simbolos)
            {
                if(s.nombre.equals(simbolo))
                    return s;
            }
            if(!a.padre.nombre.equals(""))
                a = a.padre;
            else
                return null;
        }
    }    
    public Ambito getAmbito(String nombre)
    {
        for(Ambito a: ambitos)
        {
            if(a.nombre.equals(nombre))
                return a;
        }        
        return new Ambito();
    }
    
    public void addRegla(int regla)
    {
        String s = regla + " ";
        reglas.append(s);
    }
    
    public String S()
    {
        init();
        
        String traduccion = new String("class TradC {\n\n");
        //  S.trad = “class TradC {\n\n” || Sp.trad || “main” || pari.lexema || pard.lexema || 
        //  Bloque.declaracion || Bloque.cuerpo || “}”
        if(token.tipo == Token.INT || token.tipo == Token.DOUBLE || token.tipo == Token.MAIN)
        {
            addRegla(1);    // Numero de regla en los conjuntos de prediccion
            traduccion += Sp().trad + "main() {\n";
            emparejar(Token.MAIN);
            emparejar(Token.PARI);
            emparejar(Token.PARD);
            Atributos atr_bloque = Bloque(new Atributos("","","","","","main"));
            traduccion += atr_bloque.declaracion + "\n" + atr_bloque.cuerpo + "}\n}";
        }
        else
            errorSintaxis(Token.DOUBLE, Token.INT, Token.MAIN);
        
        if(token.tipo != Token.EOF)
            errorSintaxis(Token.EOF);
        
        return traduccion;
    }
    public Atributos Sp()
    {
        Atributos atributos = new Atributos();
        if(token.tipo == Token.INT || token.tipo == Token.DOUBLE)
        {
            addRegla(2);
            atributos.trad = Funcion().trad;
            atributos.trad += Sp().trad;
        }
        else if(token.tipo == Token.MAIN)
        {
            addRegla(3);
        }
        else
            errorSintaxis(Token.DOUBLE, Token.INT, Token.MAIN);
        
        if(debug)
        {
            System.out.println("Sp declaracion: '"+atributos.declaracion + "'");
        }
        return atributos;
    }
    public Atributos Funcion()
    {
        /*
        Funcion → Tipo id pari pard {Bloque.prefijo = id.lexema} Bloque
        {Funcion.trad =
          If id.lexema = “Double” 
             “public static double “ || id.lexema || pari.lexema || pard.lexema || “{“ || Bloque.declaracion || Bloque.cuerpo || “}\n”
          Else If id.lexema = “Integer”
             “public static int “ || id.lexema || pari.lexema || pard.lexema || “{\n“ || Bloque..declaracion || Bloque.cuerpo || “}\n”
        }
        */
        Atributos atributos = new Atributos();
        if(token.tipo == Token.INT || token.tipo == Token.DOUBLE)
        {
            addRegla(4);
            Atributos atr_tipo = Tipo();
            String prefijo = new String(token.lexema);
            if (atr_tipo.trad.equals("Integer"))
                atributos.trad += "public static int " + prefijo + "() {\n";
            else
                atributos.trad += "public static double " + prefijo + "() {\n";
            emparejar(Token.ID);
            emparejar(Token.PARI);
            emparejar(Token.PARD);
            Atributos atr_bloque = Bloque(new Atributos("","","","","",prefijo));
            atributos.trad += atr_bloque.declaracion + atr_bloque.cuerpo +"}";
        }
        else
            errorSintaxis(Token.DOUBLE, Token.INT);
        
        if(debug)
        {
            System.out.println("Sp declaracion: '"+atributos.declaracion + "'");
        }
        return atributos;
    }
    public Atributos Tipo()
    {
        Atributos atributos = new Atributos();
        if(token.tipo == Token.INT)
        {
            addRegla(5);
            emparejar(Token.INT);
            atributos.trad = new String("Integer");
        }
        else if(token.tipo == Token.DOUBLE)
        {
            addRegla(6);
            emparejar(Token.DOUBLE);
            atributos.trad = new String("Double");
        }
        else
            errorSintaxis(Token.DOUBLE, Token.INT);
        
        if(debug)
        {
            System.out.println("Sp declaracion: '"+atributos.declaracion + "'");
        }
        return atributos;
    }
    public Atributos Bloque(Atributos p_atributos)
    {
        /*
        Bloque → llavei {SecInstr.prefijo = Bloque.prefijo || “_”} llaved
        {Bloque.declaracion = SecInstr.declaracion}
        {Bloque.cuerpo = SecInstr.cuerpo}
*/
        Atributos atributos = new Atributos();
        if(token.tipo == Token.LLAVEI)
        {
            // Cada vez que se abre un bloque se crea un nuevo ambito
            ambitos.add(new Ambito(p_atributos.prefijo+"_", getAmbito(p_atributos.prefijo)));
            
            addRegla(7);
            emparejar(Token.LLAVEI);
            Atributos atr_secinstr = SecInstr(new Atributos("","","","","",p_atributos.prefijo + "_"));
            emparejar(Token.LLAVED);
            
            atributos.declaracion = atr_secinstr.declaracion;
            atributos.cuerpo = atr_secinstr.cuerpo;
        }
        else
            errorSintaxis(Token.LLAVEI);
        return atributos;
    }
    
    public Atributos SecInstr(Atributos p_atributos)
    {
        Atributos atributos = new Atributos();
        if(token.tipo == Token.INT || token.tipo == Token.DOUBLE)
        {
            /*
            SecInstr → {V.prefijo = SecInstr.prefijo} V {SecInstr1.prefijo = SecInstr.prefijo} SecInstr1
            {SecInstr.declaracion = V.trad || SecInstr1.declaracion}
            {SecInstr.cuerpo = SecInstr1.cuerpo}
            */
            addRegla(8);
            Atributos atr_v = V(new Atributos("","","","","", p_atributos.prefijo));
            Atributos atr_secinstr = SecInstr(new Atributos("","","","","", p_atributos.prefijo));
            atributos.declaracion = atr_v.trad + atr_secinstr.declaracion;
            atributos.cuerpo = atr_secinstr.cuerpo;
            
        }
        else if(token.tipo == Token.LLAVEI || token.tipo == Token.ID)
        {
            /*
            SecInstr → {Instr.prefijo = SecInstr.prefijo} Instr {SecInstr1.prefijo = SecInstr.prefijo} SecInstr1
            {SecInstr.declaracion = SecInstr1.declaracion}
            SecInstr.cuerpo = Instr.trad || SecInstr1.cuerpo}
            */
            addRegla(9);
            Atributos atr_instr = Instr(new Atributos("","","","","", p_atributos.prefijo));
            atributos.declaracion = atr_instr.declaracion;
            atributos.cuerpo = atr_instr.cuerpo;
            Atributos atr_secinstr = SecInstr(new Atributos("","","","","", p_atributos.prefijo));            
            atributos.cuerpo += atr_secinstr.cuerpo;
            atributos.declaracion += atr_secinstr.declaracion;
        }
        else if(token.tipo == Token.LLAVED)
        {
            addRegla(10);            
        }
        else
            errorSintaxis(Token.LLAVEI, Token.LLAVED, Token.DOUBLE, Token.INT, Token.ID);
        if(debug)
        {
            System.out.println("SecInstr declaracion: '"+atributos.declaracion+"'");
            System.out.println("SecInstr cuerpo: '"+atributos.cuerpo+"'");
        }
        return atributos;
    }
    public Atributos V(Atributos p_atributos)
    {
        Atributos atributos = new Atributos();
        if(token.tipo == Token.INT || token.tipo == Token.DOUBLE)
        {
            /*
            V → Tipo {Var.prefijo = V.prefijo} {Var.th = Tipo.trad} Var {MV.prefijo = V.prefijo} {Mv.th = Tipo.trad} MV
            {V.trad = Var.trad || MV.trad}
            */
            addRegla(11);
            Atributos atr_tipo = Tipo();
            atributos.trad = Var(new Atributos("", atr_tipo.trad, "", "","", p_atributos.prefijo)).trad;
            //simbolos.add(new Simbolo(p_atributos.prefijo))
            atributos.trad += MV(new Atributos("", atr_tipo.trad, "", "","", p_atributos.prefijo)).trad;
        }
        else
            errorSintaxis(Token.DOUBLE, Token.INT);
        
        if(debug)
        {
            System.out.println("Sp declaracion: '"+atributos.declaracion + "'");
        }
        return atributos;
    }
    public Atributos Var(Atributos p_atributos)
    {
        Atributos atributos = new Atributos();
        if(token.tipo == Token.ID)
        {
            /*
            Var → id Array
            Si Array.trad = “”
              {Var.trad = Var.th || “ “ || Var.prefijo || id.lexema || “;\n” }
            Else
              {Var.trad = Var.th || Array.declaracion || “ “ || Var.prefijo || id.lexema || “ = new “ || Var.th || Array.cuerpo || “;\n”}
            */
            addRegla(12);
            String idlexema = token.lexema;
            // Si el simbolo ya se ha declarado antes, error semantico...
            if(existeSimbolo(p_atributos.prefijo, idlexema))
                errorSemanticoIdRepetido();
            
            emparejar(Token.ID);
            Atributos atr_array = Array();
            boolean esArray = false;
            if(atr_array.cuerpo.isEmpty() && atr_array.declaracion.isEmpty())
            {
                atributos.trad = p_atributos.th + " " + p_atributos.prefijo + idlexema + ";\n";
            }
            else
            {
                atributos.trad = p_atributos.th + atr_array.declaracion + " " + p_atributos.prefijo + idlexema + " = new " + p_atributos.th + atr_array.cuerpo + ";\n";
                esArray = true;
            }
            getAmbito(p_atributos.prefijo).simbolos.add(new Simbolo(idlexema, p_atributos.tipo, "", esArray));
            //simbolos.add(new Simbolo(p_atributos.prefijo+idlexema, p_atributos.tipo, "", esArray));
        }
        else
            errorSintaxis(Token.ID);
        if(debug)
        {
            System.out.println("Sp declaracion: '"+atributos.declaracion + "'");
        }
        return atributos;
    }
    public Atributos Array()
    {
        Atributos atributos = new Atributos();
        
        if(token.tipo == Token.CORI)
        {
            /*
            Array → cori entero cord Array1
            {Array.cuerpo = cori.lexema || entero.lexema || cord.lexema || Array1.cuerpo}
            {Array.declaracion = “[]” || Array1.declaracion}
            */
            addRegla(13);
            emparejar(Token.CORI);
            String nlexema = token.lexema;
            
            // Si el numero entre corchetes es 0, error semantico
            if(Integer.parseInt(nlexema) == 0)
                errorSemanticoArray();
            
            emparejar(Token.ENTERO);
            emparejar(Token.CORD);
            Atributos atr_array = Array();
            
            atributos.cuerpo = "[" +nlexema + "]" + atr_array.cuerpo;
            atributos.declaracion = "[]" + atr_array.declaracion;
        }
        else if(token.tipo == Token.COMA || token.tipo == Token.PYC)
        {
            addRegla(14);
        }
        else
            errorSintaxis(Token.PYC, Token.COMA, Token.CORI);
        
        return atributos;
    }
    public Atributos MV(Atributos p_atributos)
    {
        Atributos atributos = new Atributos();
        if(token.tipo == Token.COMA)
        {
            /*
            MV → coma {Var.prefijo = MV.prefijo} {Var.th = MV.th} Var {MV1.prefijo = MV.prefijo} {MV1.th = MV.th} MV1
            {MV.trad = Var.trad || MV.trad}
            */
            addRegla(15);
            emparejar(Token.COMA);
            atributos.trad = Var(new Atributos("", p_atributos.th,"","","", p_atributos.prefijo)).trad;
            atributos.trad += MV(new Atributos("", p_atributos.th,"","","", p_atributos.prefijo)).trad;
        }
        else if(token.tipo == Token.PYC)
        {
            addRegla(16);
            emparejar(Token.PYC);
        }
        else
            errorSintaxis(Token.PYC, Token.COMA, Token.CORI);
        if(debug)
        {
            System.out.println("Sp declaracion: '"+atributos.declaracion + "'");
        }
        return atributos;
    }
    public Atributos Instr(Atributos p_atributos)
    {
        Atributos atributos = new Atributos();
        
        if(token.tipo == Token.ID)
        {
            addRegla(17);
            //String simbolo = new String(p_atributos.prefijo + token.lexema);
            if(!existeSimbolo(p_atributos.prefijo, token.lexema))
                errorSemanticoIdNoDeclarado();
            
            atributos.cuerpo = p_atributos.prefijo + token.lexema + " = ";
            emparejar(Token.ID);
            emparejar(Token.ASIG);
            atributos.cuerpo += Expr(new Atributos("", "","","","", p_atributos.prefijo)).trad +";\n";
            emparejar(Token.PYC);
        }
        else if(token.tipo == Token.LLAVEI)
        {
            addRegla(18);
            Atributos atr_bloque = Bloque(new Atributos("","","","","", p_atributos.prefijo));
            atributos.declaracion = atr_bloque.declaracion;
            atributos.cuerpo = atr_bloque.cuerpo;
        }
        else
            errorSintaxis(Token.LLAVEI, Token.ID);
        
        return atributos;
    }
    public Atributos Expr(Atributos p_atributos)
    {
        Atributos atributos = new Atributos();
        if(token.tipo == Token.ID || token.tipo == Token.ENTERO || token.tipo == Token.REAL)
        {
            addRegla(19);
            atributos.trad = Term(new Atributos("", "","","","", p_atributos.prefijo)).trad;
            ExprAux(new Atributos("", "","","","", p_atributos.prefijo)); //+++++++++++++++++++ REVISAR ****************
        }
        else
            errorSintaxis(Token.ENTERO, Token.ID, Token.REAL);
        return atributos;
    }
    public Atributos ExprAux(Atributos p_atributos)
    {
        Atributos atributos = new Atributos();
        if(token.tipo == Token.ADDOP)
        {
            addRegla(20);
            emparejar(Token.ADDOP);
            Term(new Atributos("", "","","","", p_atributos.prefijo));
            ExprAux(new Atributos("", "","","","", p_atributos.prefijo));
            
        }
        else if(token.tipo == Token.PYC)
        {
            addRegla(21);            
        }
        else
            errorSintaxis(Token.ADDOP, Token.PYC);
        return atributos;
    }
    public Atributos Term(Atributos p_atributos)
    {
        // Term -> Factor { Term'.th=Factor.tipo; Term'.ch=Factor.trad; Term'.eh=Factor.esvariable} Term'
        Atributos atributos = new Atributos();
        if(token.tipo == Token.ID || token.tipo == Token.ENTERO || token.tipo == Token.REAL)
        {
            addRegla(22);
            Atributos atr_factor = Factor(new Atributos("", "","","","", p_atributos.prefijo));
            //atributos.trad = atr_factor.trad;
            Atributos a = new Atributos(atr_factor.trad, "", atr_factor.tipo,"","", p_atributos.prefijo);
            a.esvariable = atr_factor.esvariable;
            Atributos atr_termAux = TermAux(a);
            atributos.trad += atr_termAux.trad;
            atributos.tipo = atr_termAux.tipo;
        }
        else
            errorSintaxis(Token.ENTERO, Token.ID, Token.REAL);
        return atributos;
    }
    public Atributos TermAux(Atributos p_atributos)
    {
        Atributos atributos = new Atributos();
        if(token.tipo == Token.MULOP)
        {
            addRegla(23);
            emparejar(Token.MULOP);
            Atributos atr_factor = Factor(new Atributos("", "","","","", p_atributos.prefijo));
            
            //if(!p_atributos.tipo.isEmpty())
            if(atr_factor.tipo.equals(p_atributos.tipo))    // Si son dos factores del mismo tipo..
            {
                if(p_atributos.esvariable)
                    atributos.trad += "." + p_atributos.trad + "Value()";
                if(atr_factor.esvariable)
                    atributos.trad += "." + p_atributos.trad + "Value()";
            }
            else    // diferentes
            {
                // El que sea int, hay q pasarlo a double
                if(atr_factor.tipo.equals("int"))
                {
                   if(atr_factor.esvariable)
                    {
                        atr_factor.trad += ".toDouble()";
                    }
                    else
                    {
                        atr_factor.trad = "(new Integer(" + atr_factor.trad +")).toDouble()";
                    }
                }
                else
                {
                    if(p_atributos.esvariable)
                    {
                        p_atributos.trad += ".toDouble()";
                    }
                    else
                    {
                        p_atributos.trad = "(new Integer(" + atributos.trad +")).toDouble()";
                    }
                }
            }
            
            Atributos a = new Atributos(p_atributos.trad + "*" + atr_factor.trad, "","","","", p_atributos.prefijo);
            atributos.trad = TermAux(a).trad;
        }
        else if(token.tipo == Token.ADDOP || token.tipo == Token.PYC)
        {
            addRegla(24);        
            atributos.trad = p_atributos.trad;
        }
        else
            errorSintaxis(Token.MULOP, Token.ADDOP, Token.PYC);
        return atributos;
    }
    public Atributos Factor(Atributos p_atributos)
    {
        Atributos atributos = new Atributos();
        if(token.tipo == Token.REAL)
        {
            /*
            {Factor.trad = real.lexema}
            {Factor.esvariable = false;}
            {Factor.tipo = “double”}
            */
            addRegla(25);
            atributos.trad = token.lexema;
            atributos.tipo = "double";
            emparejar(Token.REAL);
        }
        else if(token.tipo == Token.ENTERO)
        {
            addRegla(26);
            atributos.trad = token.lexema;
            atributos.tipo = "int";
            emparejar(Token.ENTERO);
        }
        else if(token.tipo == Token.ID)
        {
            Simbolo s = null;
            addRegla(27);
            s = getSimbolo(getAmbito(p_atributos.prefijo).nombre, token.lexema);
            if(s.nombre.isEmpty())
                errorSemanticoIdNoDeclarado();
            
            atributos.trad = s.nombre;
            atributos.tipo = s.tipo;
            atributos.esvariable = true;
            emparejar(Token.ID);
        }
        else
            errorSintaxis(Token.ENTERO, Token.ID, Token.REAL);
        
        return atributos;
    }
}
