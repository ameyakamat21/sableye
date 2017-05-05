#define AUDIO_PIN 0
//#define LED_PIN 1
#define SENSOR_PIN 1
#define GND_PIN 3

int audioInstantAmp;
int delayUs = 1000;
int delayDelta = 1;
bool isOff = false;
int ctr = 0;



void setup() {
  // put your setup code here, to run once:

  //pinMode(AUDIO_PIN, OUTPUT);
  //pinMode(LED_PIN, OUTPUT);

  pinMode(SENSOR_PIN, INPUT);  
  //pinMode(A0, INPUT);
  pinMode(GND_PIN, OUTPUT);
  //audioInstantAmp=0;
  //Serial.begin(9600);
  //audioInstantAmp = 1;
}

void loop() {
  // put your main code here, to run repeatedly:
  digitalWrite(GND_PIN, 0); 
  delay(50);
  //analogWrite(AUDIO_PIN, 124);
  int sensorVal = analogRead(SENSOR_PIN);

  //Serial.println(sensorVal);

  if(sensorVal < 50 ) {
    
    analogWrite(AUDIO_PIN, 80);
    delay(max(sensorVal * 3/2, 10));
    analogWrite(AUDIO_PIN, 0);
   /* digitalWrite(LED_PIN, HIGH);
    digitalWrite(LED_PIN, LOW);
    digitalWrite(LED_PIN, HIGH);
    digitalWrite(LED_PIN, LOW);
    digitalWrite(LED_PIN, HIGH);
    digitalWrite(LED_PIN, LOW);*/
    
  }
  /*if(sensorVal > 100) {
    analogWrite(AUDIO_PIN, 100);
    //delay(max(sensorVal * 3/2, 10));
    //analogWrite(AUDIO_PIN, 0);
  *///}
  
}
