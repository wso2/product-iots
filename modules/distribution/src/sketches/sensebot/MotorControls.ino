
void motor_stop(){
  digitalWrite(motor_left[0], LOW); 
  digitalWrite(motor_left[1], LOW); 
  
  digitalWrite(motor_right[0], LOW); 
  digitalWrite(motor_right[1], LOW);
  unsigned long  motorStop= millis() + 25;  
  while (!(motorStop<= millis())){
  //delay 25ms
  }
}

void drive_backward(){
  //motor_stop();
  digitalWrite(motor_left[0], LOW); 
  digitalWrite(motor_left[1], HIGH); 
  
  digitalWrite(motor_right[0], LOW); 
  digitalWrite(motor_right[1], HIGH); 
}

void drive_forward(){
  //motor_stop();
  digitalWrite(motor_left[0], HIGH); 
  digitalWrite(motor_left[1], LOW); 
  
  digitalWrite(motor_right[0], HIGH); 
  digitalWrite(motor_right[1], LOW); 
}


void turn_right(){
  motor_stop();
  digitalWrite(motor_left[0], HIGH); 
  digitalWrite(motor_left[1], LOW); 
  unsigned long  motorStop= millis() + TURN_DELAY;  
  while (!(motorStop<= millis())){
  //delay 300ms
  }
  updateDirectionVariable(0);
  motor_stop();
}

void turn_left(){
  motor_stop();
  digitalWrite(motor_right[0], HIGH); 
  digitalWrite(motor_right[1], LOW);
  unsigned long  motorStop= millis() + TURN_DELAY;  
  while (!(motorStop<= millis())){
  //delay 300ms
  }
  updateDirectionVariable(0);
  motor_stop();
}
