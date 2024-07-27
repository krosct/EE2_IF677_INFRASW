import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

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
    private static Semaphore partir = new Semaphore(0); // Semáforo que controla a partida do ônibus
    private static Semaphore embarcar = new Semaphore(0); // Semáforo que controla o embarque dos passageiros no ônibus
    private static Semaphore areaEmbarque = new Semaphore(1, false); // Semáforo que controla a área de embarque (passageiros que chegaram antes do ônibus chegar)
    private static Semaphore atom = new Semaphore(1); // Semáforo que controla a atomicidade de algumas operações
    private static int lotacao = 50; // Quantidade de assentos do ônibus
    private static int espera = 0;  // Quantidade de passageiros na área de embarque do ônibus
    private static int parada = 0;  // Quantidade de passageiros na parada do ônibus
    private static int embarcado = 0; // Quantidade de passageiros embarcados no ônibus
    private static int periodoMinOnibus = 1000; // Limite inferior de tempo para a partida do ônibus
    private static int periodoMaxOnibus = 3000; // Limite superior de tempo para a partida do ônibus
    private static int qtdPassagem = 10; // Quantidade de vezes que o ônibus passará na parada

    public static void generalPrint(String name, String text) {
        System.out.printf("%-17s %-25s Parada:%3d, Espera:%3d, Embarcado:%3d%n", name, text, parada, espera, embarcado);
    }

    @Override
    public void run() {
        try {
            atom.acquire();
            generalPrint(Thread.currentThread().getName(), "iniciará sua jornada.");
            atom.release();
            

            while (qtdPassagem > 0) { // O ônibus irá passar na parada "qtdPassagem" vezes
            
                atom.acquire();
                areaEmbarque.acquire(); // Proibe que novos passageiros entrem na área de embarque
                generalPrint(Thread.currentThread().getName(), "chegou na parada.");
                

                if (espera == 0) { // Se não houve nenhuma passageiro esperando pelo ônibus, o ônibus segue seu caminho
                    generalPrint(Thread.currentThread().getName(), "partiu SEM passageiros.");
                    atom.release();
                    

                } else { // Se houver passageiros à espera do ônibus, então os passageiros embarcam e o ônibus parte
                    embarcar.release(); // Permite que os passageiros da área de embarque possam embarcar no ônibus
                    atom.release();
                    partir.acquire(); // Aguarda até que o ônibus fique cheio ou que todos os passageiros que estavam à espera do ônibus embarquem
                    
                    atom.acquire();
                    generalPrint(Thread.currentThread().getName(), "partiu COM passageiros.");
                    embarcado = 0; // Reseta o número de passageiros embarcados
                    atom.release();


                }
                areaEmbarque.release(); // Permite que novos passageiros entrem na área de embarque
                
                Thread.sleep((long) (Math.random() * (periodoMaxOnibus - periodoMinOnibus) + periodoMinOnibus)); // Tempo de simulação do tempo de viagem do ônibus
                qtdPassagem = qtdPassagem - 1; // Diminiu uma quantidade de vezes que o ônibus passará na parada
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }
    
    public static void embarcar(Passageiro passageiro) throws InterruptedException {
        // ÁREA DA PARADA
        atom.acquire();
        parada++; // mais um passageiro na parada de ônibus
        generalPrint(Thread.currentThread().getName(), "chegou na parada.");
        atom.release();
        
        
        areaEmbarque.acquire(); // CATRACA DA ÁREA ELEGÍVEL PARA EMBARQUE: PASSAGEIROS QUE CHEGARAM ANTES DO ÔNIBUS CHEGAR
        espera++; // mais um passageiro na área de embarque
        areaEmbarque.release();
        
        
        embarcar.acquire(); // CATRACA PARA EMBARCAR NO ÔNIBUS
        atom.acquire();
        embarcado++; // mais um passageiro embarcou no ônibus
        espera--; // menos um passageiro na área de espera do ônibus
        parada--; // menos um passageiro na parada de ônibus
        generalPrint(Thread.currentThread().getName(), "embarcou.");
        if(embarcado >= lotacao || espera <= 0) { // Se o ônibus estiver cheio ou a espera vazia o ônibus pode partir
            partir.release();
            generalPrint(Thread.currentThread().getName(), "\"Aceleraaa motorrr!\"");
        } else { // Caso o ônibus não esteja cheio nem a espera vazia, outro passageiro pode embarcar no ônibus
            embarcar.release();
        }
        atom.release();
    }
}

public class Transporte {
    public static void main(String[] args) throws InterruptedException {
        int qtdPassageiros = 337; // Quantidade de passageiros que embarcarão no ônibus
        
        // Onibus
        Thread onibus = new Thread(new Onibus(), "Ônibus 1"); // Cria a instância do ônibus
        
        // Passageiros
        List<Thread> passageiros = new ArrayList<>(); // Cria lista de passageiros (threads)
        for(int i = 0; i < qtdPassageiros; ++i) {
            passageiros.add(new Thread(new Passageiro(), "Passageiro " + i)); // Cria as instâncias dos passageiros
            if (i == qtdPassageiros/10) {
                onibus.start(); // Inicia a thread do ônibus
            }
            passageiros.get(i).start(); // Inicia a thread do passageiro
        }
    }
}