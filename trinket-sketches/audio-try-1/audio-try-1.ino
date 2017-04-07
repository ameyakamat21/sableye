#define AUDIO_PIN 4

int audioInstantAmp;
int delayUs = 1000;
int delayDelta = 1;
bool isOff = false;
int ctr = 0;

void setup() {
  pinMode(AUDIO_PIN, OUTPUT);
  audioInstantAmp=0;
}

void loop() {
  ctr++;
  audioInstantAmp = 1 - audioInstantAmp;

  if(true) {
    digitalWrite(AUDIO_PIN, audioInstantAmp);
  }
  
  if(delayUs > 5000) {
    delayDelta = -4;
  } else if(delayUs < 1) {
    delayDelta = 4;
  } 

  int modThing = random(1,5)*25;
  if(ctr % modThing == 0) {
    isOff = !isOff;
  }
  
  delayUs += delayDelta;
  delayMicroseconds(delayUs);
}
