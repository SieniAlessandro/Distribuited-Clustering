import org.python.util.PythonInterpreter;
import org.python.core.*;
public class example {

    public static void main(String[] args) throws PyException {
        // Prints "Hello, World" to the terminal window.
        PythonInterpreter interp = new PythonInterpreter();
        System.out.println("Hello, World from Java");
        interp.execfile("../python/examplescript.py");
        PyFunction fun = (PyFunction)interp.get("fun", PyFunction.class);
        PyFunction fun2 = (PyFunction)interp.get("fun2", PyFunction.class);
        PyFunction prod = (PyFunction)interp.get("product", PyFunction.class);

        String ret = ((PyString)fun.__call__()).asString();

        //String ret = Pyret.asString();
        System.out.println(ret);
        ret = ((PyString)fun2.__call__()).asString();
        System.out.println(ret);
        PyInteger _p = (PyInteger)(prod.__call__(new PyInteger(3),new PyInteger(5)));
        int p = (int)(_p.getValue());
        System.out.println(p);
    }

}
