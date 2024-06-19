import java.util.ArrayList;
import java.util.List;

class Pessoa implements Runnable {
    private boolean tipoOperacao; // true para depositar, false para sacar
    private double quantia;

    public Pessoa(boolean tipoOperacao, double quantia) {
        this.tipoOperacao = tipoOperacao;
        this.quantia = quantia;
    }

    @Override
    public void run() {
        System.out.println("Eu sou " + Thread.currentThread().getName() + ". Tipo de operação: " + tipoOperacao + ". Quantia: " + quantia);
    }
}

public class Banco {
    public static void main(String[] args) {
        List<Thread> pessoas = new ArrayList<>();

        pessoas.add(new Thread(new Pessoa(true, 277), "Pessoa1"));
        pessoas.add(new Thread(new Pessoa(false, 6000.0), "Pessoa5"));
        pessoas.add(new Thread(new Pessoa(true, 1500.0), "Pessoa2"));
        pessoas.add(new Thread(new Pessoa(false, 6765.0), "Pessoa6"));
        pessoas.add(new Thread(new Pessoa(false, 3456.0), "Pessoa7"));
        pessoas.add(new Thread(new Pessoa(true, 9988.0), "Pessoa3"));
        pessoas.add(new Thread(new Pessoa(true, 7299.0), "Pessoa4"));
        pessoas.add(new Thread(new Pessoa(false, 211.0), "Pessoa8"));

        // Inicia todas as threads
        for (Thread t: pessoas) {
            t.start();
        }

        // Espera todas as threads terminarem
        for (Thread t: pessoas) {
            try {
                t.join();
            } catch (InterruptedException e) {
                t.interrupt();
            }
        }
    }
}
