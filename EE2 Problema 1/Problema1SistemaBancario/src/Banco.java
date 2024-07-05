import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class Conta {
    private double saldo;
    private Lock lock = new ReentrantLock();
    private Condition saldoSuficiente = lock.newCondition();

    public Conta(double saldoInicial) {
        this.saldo = saldoInicial;
    }

    public void depositar(double quantia) {
        lock.lock();
        try {
            saldo += quantia;
            System.out.println(Thread.currentThread().getName() + " (+++) R$" + quantia + ". Saldo: R$" + saldo + ".");
            saldoSuficiente.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public void sacar(double quantia) throws InterruptedException {
        lock.lock();
        try {
            while (saldo < quantia) {
                System.out.println(Thread.currentThread().getName() + " (~~~) R$" + quantia + ". Saldo: R$" + saldo + ".");
                saldoSuficiente.await();
            }
            saldo -= quantia;
            System.out.println(Thread.currentThread().getName() + " (---) R$" + quantia + ". Saldo: R$" + saldo + ".");
        } finally {
            lock.unlock();
        }
    }

    public double getSaldo() {
        lock.lock();
        try {
            return saldo;
        } finally {
            lock.unlock();
        }
    }
}

class Pessoa implements Runnable {
    private Conta conta;
    private boolean tipoOperacao; // true para depositar, false para sacar
    private double quantia;

    public Pessoa(Conta conta, boolean tipoOperacao, double quantia) {
        this.conta = conta;
        this.tipoOperacao = tipoOperacao;
        this.quantia = quantia;
    }

    @Override
    public void run() {
        try {
            if (tipoOperacao) {
                conta.depositar(quantia);
            } else {
                conta.sacar(quantia);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

public class Banco {
    public static void main(String[] args) {
        Conta contaCompartilhada = new Conta(1000);
        List<Thread> pessoas = new ArrayList<>();

        pessoas.add(new Thread(new Pessoa(contaCompartilhada, true, 277), "Pessoa1"));
        pessoas.add(new Thread(new Pessoa(contaCompartilhada, false, 6000.0), "Pessoa5"));
        pessoas.add(new Thread(new Pessoa(contaCompartilhada, true, 1500.0), "Pessoa2"));
        pessoas.add(new Thread(new Pessoa(contaCompartilhada, false, 6765.0), "Pessoa6"));
        pessoas.add(new Thread(new Pessoa(contaCompartilhada, false, 3456.0), "Pessoa7"));
        pessoas.add(new Thread(new Pessoa(contaCompartilhada, true, 9988.0), "Pessoa3"));
        pessoas.add(new Thread(new Pessoa(contaCompartilhada, true, 7299.0), "Pessoa4"));
        pessoas.add(new Thread(new Pessoa(contaCompartilhada, false, 211.0), "Pessoa8"));
        pessoas.add(new Thread(new Pessoa(contaCompartilhada, false, 100000.0), "Pessoa9"));
        pessoas.add(new Thread(new Pessoa(contaCompartilhada, false, 100000.0), "Pessoa9"));

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

        System.out.println("Saldo final: R$" + contaCompartilhada.getSaldo() + ".");
    }
}
