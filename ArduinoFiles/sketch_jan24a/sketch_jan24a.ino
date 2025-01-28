// Define LED pins
const int ledGaraje = 27;   // GPIO2 conectado a D2 en la placa
const int ledBano1 = 4;    // GPIO4 conectado a D4 en la placa
const int ledBano2 = 25;    // GPIO5 conectado a D5 en la placa
const int ledSala = 14;    // GPIO12 conectado a D12 en la placa
const int ledCuarto1 = 12; // GPIO14 conectado a D14 en la placa
const int ledCuarto2 = 2; // GPIO27 conectado a D27 en la placa
const int ledCocina = 26;  // GPIO26 conectado a D26 en la placa
const int ledCorredor = 5;// GPIO25 conectado a D25 en la placa

// Variables to hold the states of each LED
int ledStateGaraje = 0;
int ledStateBano1 = 0;
int ledStateBano2 = 0;
int ledStateSala = 0;
int ledStateCuarto1 = 0;
int ledStateCuarto2 = 0;
int ledStateCocina = 0;
int ledStateCorredor = 0;

String mensajeServidor;
void setup() {
  Serial.begin(9600);
  pinMode(ledGaraje, OUTPUT);
  pinMode(ledBano1, OUTPUT);
  pinMode(ledBano2, OUTPUT);
  pinMode(ledSala, OUTPUT);
  pinMode(ledCuarto1, OUTPUT);
  pinMode(ledCuarto2, OUTPUT);
  pinMode(ledCocina, OUTPUT);
  pinMode(ledCorredor, OUTPUT);

}

void loop() {
  mensajeServidor=Serial.readStringUntil('\n');
  //puede recibir o GARAJE, BANO1, BANO2, SALA, CORREDOR, COCINA, CUARTO1, CUARTO2
  if (mensajeServidor == "GARAJE") {
    ledStateGaraje = invertSignal(ledStateGaraje);
    digitalWrite(ledGaraje, ledStateGaraje);
  } else if (mensajeServidor == "BANO1") {
    ledStateBano1 = invertSignal(ledStateBano1);
    digitalWrite(ledBano1, ledStateBano1);
  } else if (mensajeServidor == "BANO2") {
    ledStateBano2 = invertSignal(ledStateBano2);
    digitalWrite(ledBano2, ledStateBano2);
  } else if (mensajeServidor == "SALA") {
    ledStateSala = invertSignal(ledStateSala);
    digitalWrite(ledSala, ledStateSala);
  } else if (mensajeServidor == "CUARTO1") {
    ledStateCuarto1 = invertSignal(ledStateCuarto1);
    digitalWrite(ledCuarto1, ledStateCuarto1);
  } else if (mensajeServidor == "CUARTO2") {
    ledStateCuarto2 = invertSignal(ledStateCuarto2);
    digitalWrite(ledCuarto2, ledStateCuarto2);
  } else if (mensajeServidor == "COCINA") {
    ledStateCocina = invertSignal(ledStateCocina);
    digitalWrite(ledCocina, ledStateCocina);
  } else if (mensajeServidor == "CORREDOR") {
    ledStateCorredor = invertSignal(ledStateCorredor);
    digitalWrite(ledCorredor, ledStateCorredor);
  }

}

int invertSignal(int ledSignal){
  if(ledSignal==1){
    return 0;
  }else{
    return 1;
  }
}
