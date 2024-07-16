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
    private static int lotacao = 20; // Quantidade de assentos máximo do ônibus
    private static Semaphore partir = new Semaphore(0); // Semáforo que controla a partida do ônibus
    private static Semaphore chegar = new Semaphore(1); // Semáforo que controla a chegada de passageiros na parada
    private static Semaphore embarcar = new Semaphore(0); // Semáforo que controla o embarque dos passageiros no ônibus
    private static Semaphore embarqueSafe = new Semaphore(lotacao); // Semáforo que controla o embarque dos passageiros no ônibus // ! ajustar a descrição
    private static Semaphore areaEmbarque = new Semaphore(lotacao); // Semáforo que controla o embarque dos passageiros no ônibus // ! ajustar a descrição
    private static Semaphore move = new Semaphore(1); // Semáforo que controla o embarque dos passageiros no ônibus // ! ajustar a descrição
    private static int parada = 0; // Quantidade de passageiros esperando o ônibus na parada
    private static int embarcado = 0; // Quantidade de passageiros embarcados no ônibus
    private static int periodoMinOnibus = 1000; // Limite inferior de tempo para a partida do ônibus
    private static int periodoMaxOnibus = 3000; // Limite superior de tempo para a partida do ônibus
    private static int qtdPassagem = 10; // Quantidade de vezes que o ônibus passará na parada
    private static int tempoEmbarque = 100; // Tempo para os passageiros embarcarem no ônibus
    private static int espera = 0;

    public void run() {
        try {
            System.out.printf("%-17s %-25s Parada:%d, Embarcado:%d %d %d%n", Thread.currentThread().getName(), "iniciará sua jornada.", parada, embarcado, embarcar.availablePermits(), embarqueSafe.availablePermits());
            while (qtdPassagem > 0) {
                areaEmbarque.drainPermits();
                move.acquire();
                embarcar.release();
                System.out.printf("%-17s %-25s Parada:%d, Embarcado:%d %d %d%n", Thread.currentThread().getName(), "chegou na parada.", parada, embarcado, embarcar.availablePermits(), embarqueSafe.availablePermits());
                move.release();
                partir.tryAcquire(tempoEmbarque, TimeUnit.MILLISECONDS); // Espera até todos os passageiros em espera embarcarem para partir
                embarcar.acquire();
                move.acquire();
                if (embarcado <= 0) {
                    // Se não houver passageiros na parada, o ônibus irá partir imediatamente
                    System.out.printf("%-17s %-25s Parada:%d, Embarcado:%d %d %d%n", Thread.currentThread().getName(), "partiu SEM passageiros.", parada, embarcado, embarcar.availablePermits(), embarqueSafe.availablePermits());
                } else {
                    // Se houver passageiros na parada, então os passageiros embarcam e o ônibus parte
                    System.out.printf("%-17s %-25s Parada:%d, Embarcado:%d %d %d%n", Thread.currentThread().getName(), "partiu COM passageiros.", parada, embarcado, embarcar.availablePermits(), embarqueSafe.availablePermits());
                }
                embarcado = 0;
                move.release();
                areaEmbarque.release(lotacao - espera);
                Thread.sleep((long) (Math.random() * (periodoMaxOnibus - periodoMinOnibus) + periodoMinOnibus)); // Tempo de simulação tempo de viagem
                qtdPassagem = qtdPassagem - 1; // Diminiu uma quantidade de vezes que o ônibus passará na parada
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    public static void embarcar(Passageiro passageiro) throws InterruptedException {
        move.acquire();
        parada++;
        System.out.printf("%-17s %-25s Parada:%d, Embarcado:%d %d %d%n", Thread.currentThread().getName(), "chegou na parada.", parada, embarcado, embarcar.availablePermits(), embarqueSafe.availablePermits());
        System.out.println("                                                                                     Sou " + Thread.currentThread().getName() + " estou esperando vaga.");
        move.release();
        areaEmbarque.acquire();
        move.acquire();
        espera++;
        parada--;
        move.release();
        System.out.println("                                                                                     Sou " + Thread.currentThread().getName() + " estou esperando para embarcar.");
        embarcar.acquire();
        move.acquire();
        embarcado++;
        espera--;
        System.out.printf("%-17s %-25s Parada:%d, Embarcado:%d %d %d%n", Thread.currentThread().getName(), "embarcou.", parada, embarcado, embarcar.availablePermits(), embarqueSafe.availablePermits());
        if(embarcado >= lotacao || parada <= 0) {
            // Se o ônibus estiver cheio ou a parada vazia o ônibus pode partir
            partir.release();
        }
        embarcar.release();
        move.release();
    }
}

public class Transporte {
    public static void main(String[] args) throws InterruptedException {
        int qtdPassageiros = 77; // Quantidade de passageiros que tentarão embarcar no ônibus
        
        
        // Passageiros
        List<Thread> passageiros = new ArrayList<>(); // Cria lista de passageiros (threads)
        for(int i = 0; i < qtdPassageiros; ++i) {
            passageiros.add(new Thread(new Passageiro(), "Passageiro " + i)); // Cria as instâncias dos passageiros
        }
        for (Thread passageiro: passageiros) {
            passageiro.start(); // Inicia todas as threads dos passageiros
            // Thread.sleep(83);
        }
        
        
        // Onibus
        Thread onibus = new Thread(new Onibus(), "Ônibus 1"); // Cria a instância do ônibus
        onibus.start(); // Inicia a thread do ônibus
    }
}