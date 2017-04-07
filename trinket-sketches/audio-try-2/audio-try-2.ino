#define AUDIO_PIN 3
#define LED_PIN 1
#define SENSOR_PIN 2

int audioInstantAmp;
int delayUs = 1000;
int delayDelta = 1;
bool isOff = false;
int ctr = 0;



void setup() {
  // put your setup code here, to run once:

  pinMode(AUDIO_PIN, OUTPUT);
  pinMode(LED_PIN, OUTPUT);

  pinMode(SENSOR_PIN, INPUT);  
  pinMode(A0, INPUT);
  //audioInstantAmp=0;
  Serial.begin(9600);
  audioInstantAmp = 1;
}

void loop() {
  // put your main code here, to run repeatedly:

  delay(50);
  //analogWrite(AUDIO_PIN, 124);
  int sensorVal = analogRead(A0);

  Serial.println(sensorVal);

  if(sensorVal < 100) {
    
    analogWrite(AUDIO_PIN, 80);
    delay(max(sensorVal * 3/2, 10));
    analogWrite(AUDIO_PIN, 0);
  }
  
}
