/**
Group Members
1. Nico Sibiya - u24667642
2. Tshegofatso Kungwane - u23605032
3. Liam du Toit - u
 **/
public class BakeryLock implements Lock 
{

    private final int n;
    private final VolatileBoolean[] flag;
    private final VolatileInt[] label;

    public BakeryLock(int n) 
    {
        this.n = n;
        flag = new VolatileBoolean[n];
        label = new VolatileInt[n];
        
        for(int i = 0 ; i < n ;i++){
            flag[i] = new VolatileBoolean(false);
            label[i] = new VolatileInt(0);
        }

    }

    @Override
    public void lock(int threadId) 
    {
        //step 1: Announce that this thread is choosing a Number
        flag[threadId].value = true;
        
        //step 2: Choose a number greater than every current label
        label[threadId].value = maxLabel() + 1;
        
        //step 3: Finished choosing
        flag[threadId].value = false;
        
        //step 4: Wait for every other thread
        for(int j = 0 ; j < n ; j++){
            if( j == threadId){
                continue;
            }
            
            //wait while thread j has priority over Us
            while(label[j].value !=0 &&
                    (label[j].value < label[threadId].value ||
                    (label[j].value == label[threadId].value && j < threadId))){
                        Thread.yield();
                    }
        }

    }

    @Override
    public void unlock(int threadId) 
    {
        //set ticket to 0 to indicate that
        //This thread has left the critical section 
        label[threadId].value = 0;
    }
    
    private int maxLabel() 
    {
        int max = 0;

        for (int i = 0; i < n; i++) 
        {
            if (label[i].value > max) 
            {
                max = label[i].value;
            }
        }

        return max;
    }
}