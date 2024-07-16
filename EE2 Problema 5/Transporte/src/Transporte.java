import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

class Passageiro implements Runnable {
    @Override
    public void run() {
        try {
            Onibus.embarcar(this); // Passageiro irá para a parada de ônibus para pegar um ônibus
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }
}

class Onibus implements Runnable {
    private static int lotacao = 50;
    private static Semaphore partir = new Semaphore(0);
    private static Semaphore proxima = new Semaphore(1);
    private static Semaphore embarcar = new Semaphore(1);
    private static int naParada = 0;
    private static int embarcado = 0;
    private static int periodoMinOnibus = 1000;
    private static int periodoMaxOnibus = 3000;
    private static int qtdPassagem = 10;

    public void run() {
        try {
            System.out.println(Thread.currentThread().getName() + " iniciará sua jornada.");
            while (qtdPassagem > 0) {
                if (!partir.tryAcquire(500, TimeUnit.MILLISECONDS)) { // Espera até todos os passageiros em espera embarcarem para partir
                    System.out.println("Não tinha ninguém na parada, então " + Thread.currentThread().getName() + " passou direto!");
                } else {
                    System.out.println(Thread.currentThread().getName() + " partindo com " + embarcado + " passageiro(s).");
                }
                embarcado = 0;
                Thread.sleep((long) (Math.random() * (periodoMaxOnibus - periodoMinOnibus) + periodoMinOnibus)); // Simula tempo de viagem
                qtdPassagem--;
                System.out.println(Thread.currentThread().getName() + " chegou na parada.");
                embarcar.release();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    public static void embarcar(Passageiro passageiro) throws InterruptedException {
        proxima.acquire();
        System.out.println(Thread.currentThread().getName() + " chegou à parada de ônibus. (" + ++naParada + ")");
        proxima.release();
        embarcar.acquire();
        System.out.println(Thread.currentThread().getName() + " embarcou no ônibus. " + "(" + ++embarcado + "/" + lotacao + ")");
        naParada--;
        if(embarcado >= lotacao || naParada <= 0 ) {
            partir.release();
        } else {
            embarcar.release();
        }
    }
}

public class Transporte {
    public static void main(String[] args) {
        int qtdPassageiros = 112; // Quantidade de passageiros que tentarão embarcar no ônibus
        
        
        // Passageiros
        List<Thread> passageiros = new ArrayList<>(); // Cria lista de passageiros (threads)
        for(int i = 0; i < qtdPassageiros; ++i) {
            passageiros.add(new Thread(new Passageiro(), "Passageiro " + i)); // Cria as instâncias dos passageiros
        }
        for (Thread passageiro: passageiros) {
            passageiro.start(); // Inicia todas as threads dos passageiros
        }


        // Onibus
        Thread onibus = new Thread(new Onibus(), "Ônibus 1"); // Cria a instância do ônibus
        onibus.start(); // Inicia a thread do ônibus
    }
}