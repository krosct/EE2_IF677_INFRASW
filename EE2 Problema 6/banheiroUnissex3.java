import java.util.Objects;
import java.util.concurrent.Semaphore;

public class banheiroUnissex3 {

    //Essa é a classe banheiro ela que tem as funções que fazem o controle de quem entra, quem sai e limite de pessoas
    public static  class Banheiro {
        public int usuarios = 0;//Conta quantas pessoas estão no banheiro (serve apenas para fins visuais de print)
        public String sexoUsando= ""; //registra qual sexo está usando o banheiro e estabele "" para quando tiver vazio

        private static final Semaphore semaphoreBanheiro = new Semaphore(3);//Limita a quantidade de pessoas no banheiro
        private static final Semaphore semaphoreZerou= new Semaphore(1); //Faz a troca de sexo quando o banheiro zera os usuários
        private static final Semaphore semaphoreOlhar= new Semaphore(1); //Controla a verificação da situação do banheiro


        //Função principal
        public void usarBanheiro(String sexo,int tempo) throws InterruptedException {

            //Essa função controla quem vai entrar no banheiro
            verificarSexo2(sexo);

            //Tanto esse print como o debaixo dentro do IF, assim como o "usuarios" tem como intuito fins didáticos, explicarei melhor na apresentação
            System.out.println(">>>Uma pessoa do sexo "+sexo+" entrou no banheiro");
            usuarios++;
            if (usuarios==3){//semaphoreBanheiro.availablePermits() == 0
                System.out.println("||Banheiro atingiu o limite!||");
            }
            //Simula o tempo de uso do banheiro
            Thread.sleep(tempo);
            System.out.println("<<<Uma pessoa do sexo "+sexo+" saiu do banheiro ");
            usuarios--;
            if (usuarios ==  0){
                System.out.println("||Banheiro Vazio pode entrar qualquer um||");
            }
            //Libera o banheiro
            semaphoreBanheiro.release();
            //Limpa o status do uso do banheiro e libera o semáforo
            if (semaphoreBanheiro.availablePermits() ==  3){
                sexoUsando = "";
                semaphoreZerou.release();
            }

        }

        //Essa função serve apenas para retornar se o status do sexo é igual ou vazio
        public boolean paridadeSexo(String sexo){
            if((Objects.equals(sexoUsando, ""))){
                return true;
            }
            else if((Objects.equals(sexo, sexoUsando))){
                return true;
            }
            else{
                return false;
            }
        }

        //Função que faz o controle da entrada no banheiro
        public void verificarSexo2(String sexo) throws InterruptedException {
            System.out.println("---Uma pessoa do sexo "+sexo+" chegou no banheiro");
            //Controla a verificação da situação do banheiro
            semaphoreOlhar.acquire();

            //Verifica se o banheiro já está sendo usado
            if(semaphoreBanheiro.availablePermits() !=  3){
                //Verifica se o sexo que está no banheiro é o mesmo da pessoa
                if(paridadeSexo(sexo)){
                    // Verifica se  não está lotado
                    if(semaphoreBanheiro.availablePermits() != 0){
                        semaphoreBanheiro.acquire();
                        sexoUsando = sexo;
                        semaphoreOlhar.release();
                    }
                    else{
                        semaphoreZerou.acquire();
                        semaphoreBanheiro.acquire();
                        sexoUsando = sexo;
                        semaphoreOlhar.release();

                    }
                }
                else{
                    semaphoreZerou.acquire();
                    semaphoreBanheiro.acquire();
                    sexoUsando = sexo;
                    semaphoreOlhar.release();

                }
            }
            else{
                semaphoreBanheiro.acquire();
                semaphoreZerou.acquire();
                sexoUsando = sexo;
                semaphoreOlhar.release();
            }
        }


    }

    //Classe que representa o usuário do banheiro, vai receber o sexo, o banheiro a ser usado e o tempo de uso
    public  static  class  Usuario implements Runnable{
        private Banheiro banheiro;
        private String sexo;
        private int tempo;

        public Usuario(String sexo,Banheiro banheiro,int tempo){
            this.sexo = sexo;
            this.banheiro =  banheiro;
            this.tempo = tempo;
        }

        public void run(){
            try {
                banheiro.usarBanheiro(sexo,tempo);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Banheiro banheiro = new Banheiro();
        String sexo = "";
        //Aqui eu coloquei só pra variar se o número for par gera um sexo se for impar gera outro
        for (int i = 0; i < 100; i++){
            if (i % 2 == 0){
                sexo = "feminino";
            }
            else{
                sexo = "masculino";
            }
            //Usei uma continha pra variar o tempo só pra simular diferentes tempos
            Usuario usuario = new Usuario(sexo,banheiro,(i+1)*200);
            Thread t1 = new Thread(usuario);
            t1.start();

        }
    }
}
