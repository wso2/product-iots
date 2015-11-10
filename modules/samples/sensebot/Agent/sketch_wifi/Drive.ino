


void updateDirectionVariable(int motionDir){
  motion_global = motionDir;
}


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

void drive(){
  switch(motion_global){           
     case 1 : drive_forward();                     
               break;
     case 2 : drive_backward();            
               break;
     case 3 : turn_left();           
               break;
     case 4 : turn_right();
               break;                   
     case 5 : 
              motor_stop();          
               break;  
  }
}
