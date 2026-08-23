/**
Group Members
1. Nico Sibiya - u24667642
2. Tshegofatso Kungwane - u23605032
3. Liam du Toit - u
 **/
public class FilterLock implements Lock 
{

    private final int n;
    private final VolatileInt[] level;
    private final VolatileInt[] victim;

    public FilterLock(int n) 
    {
        this.n = n;
        level = new VolatileInt[n];
        victim = new VolatileInt[n];
        
        for(int i = 0 ; i < n ; i++){
            level[i] = new VolatileInt(0);
        }
        
        for(int i = 0 ; i < n ;i++){
            victim[i] = new VolatileInt(-1);
        }
    
    }

    @Override
    public void lock(int threadId) 
    {   
        // Thread must pass through levels 1 to n-1
        for (int L = 1; L < n; L++) 
        {
            // Move this thread to level L
            level[threadId].value = L;

            // Make this thread the victim at this level
            victim[L].value = threadId;

            // Wait while:
            // 1. another thread is at this level or higher
            // 2. and we are still the victim
            boolean anotherThreadAtLevel;
            
            do 
            {
                anotherThreadAtLevel = false;

                for (int k = 0; k < n; k++) 
                {
                    if (k != threadId && level[k].value >= L) 
                    {
                        anotherThreadAtLevel = true;
                        break;
                    }
                }

                if (anotherThreadAtLevel && victim[L].value == threadId) 
                {
                    Thread.yield();
                }

            } while (anotherThreadAtLevel && victim[L].value == threadId);
        }
    }

    @Override
    public void unlock(int threadId) 
    {
        //leave the lock by returning to level 0
        level[threadId].value = 0;
        
    }
}