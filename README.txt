Group Members
1. Nico Sibiya - u24667642
2. Tshegofatso Kungwane - u23605032
3. Liam du Toit - u

Concurrent Lock Algorithms

This project implements two software-based mutual exclusion algorithms for concurrent   programs:

Lamport's Bakery Lock
Filter Lock

Both algorithms implement the common `Lock` interface and use `volatile` variables to allow multiple threads to safely observe changes made by other threads.


1. Project Structure

The project contains the following classes:
BakeryLock. 
FilterLock. 

VolatileInt. 
VolatileBoolean. 


Lock

The common interface used by both algorithms:

public interface Lock 
{
    void lock(int threadId);
    void unlock(int threadId);
}


Each lock therefore provides two operations:

-`lock(threadId)` — attempts to enter the critical section.
-`unlock(threadId)` — leaves the critical section.



2. Volatile Variables

The algorithms use two wrapper classes.

**VolatileInt**


public class VolatileInt 
{
    public volatile int value;

    public VolatileInt(int value) 
    {
        this.value = value;
    }
}


-`VolatileInt` stores an integer whose value can be accessed by multiple threads.

The `volatile` keyword ensures that changes to the variable are visible to other threads.

It is used for:

* Bakery Lock ticket numbers
* Filter Lock levels
* Filter Lock victims

 

**VolatileBoolean**


public class VolatileBoolean 
{
    public volatile boolean value;

    public VolatileBoolean(boolean value) 
    {
        this.value = value;
    }
}


-`VolatileBoolean` is used by the Bakery Lock to indicate whether a thread is currently choosing its ticket.


3. Bakery Lock

The Bakery Lock is based on the idea of taking a number at a bakery.

When customers arrive at a bakery, they take a number and wait for the lowest number to be served.

The same idea is applied to threads.

Each thread receives a ticket number.

The thread with the smallest ticket gets access to the critical section first.

If two threads have the same ticket number, the thread with the smaller thread ID gets priority.


3.1 Bakery Lock Variables

The class contains:


private final int n;
private final VolatileBoolean[] flag;
private final VolatileInt[] label;


**`n`

Stores the number of threads using the lock.

For example:


BakeryLock lock = new BakeryLock(4);


means there are four possible threads:

Thread 0
Thread 1
Thread 2
Thread 3


**`flag`


flag[i]

indicates whether thread `i` is currently choosing a ticket.


false -> thread is not choosing
true  -> thread is choosing


** `label`

label[i]

stores the ticket number belonging to thread `i`.

A value of:

0 -> means the thread is not currently competing for the critical section.


4. Bakery Lock Algorithm

The `lock()` operation follows four main steps.

*Step 1 — Announce that the thread is choosing


flag[threadId].value = true;


This tells other threads:

> "I am currently choosing my ticket."



Step 2 — Choose a ticket

label[threadId].value = maxLabel() + 1;

The thread looks at all current ticket numbers and chooses a number greater than the maximum.

For example:


Thread 0 → 3
Thread 1 → 7
Thread 2 → 5

The maximum is:
7

Therefore a new thread chooses:

8

Step 3 — Finish choosing


flag[threadId].value = false;

This tells other threads:

> "I have finished choosing my ticket."


Step 4 — Wait for threads with priority

The thread checks every other thread.

It waits if another thread:

1. Has a smaller ticket number, or
2. Has the same ticket number but a smaller thread ID.

The priority rule is:


(label, threadId)


The smaller pair has priority.

For example:

 
Thread 0 → (5, 0)
Thread 1 → (5, 1)
 

Thread 0 has priority because:

 
(5, 0) < (5, 1)
 
5. Bakery Lock Example

Suppose three threads have:

 
Thread 0 → label 4
Thread 1 → label 2
Thread 2 → label 6
 

The order will be:

 
Thread 1
    ↓
Thread 0
    ↓
Thread 2
 

because:

 
2 < 4 < 6
 

Thread 2 therefore waits until threads 1 and 0 have finished.

 

  6. Bakery Lock Unlock

When a thread leaves the critical section:

  
label[threadId].value = 0;
 

A label of `0` means:

> "This thread is no longer competing for the lock."

For example:

 
Before:

Thread 0 → 3
Thread 1 → 5
Thread 2 → 7

After Thread 1 unlocks:

Thread 0 → 3
Thread 1 → 0
Thread 2 → 7
 

 

  7. Filter Lock

The Filter Lock is another mutual exclusion algorithm designed for multiple threads.

Instead of giving every thread a ticket, the Filter Lock makes threads pass through a sequence of levels.

For `n` threads, there are:

 
n - 1
 

levels.

For example, with four threads:

 
Level 1
   ↓
Level 2
   ↓
Level 3
   ↓
Critical Section
 

Each level filters out competing threads.

 

  8. Filter Lock Variables

The Filter Lock contains:

  
private final int n;
private final VolatileInt[] level;
private final VolatileInt[] victim;
 

 

  `level`

  
level[i]
 

stores the current level of thread `i`.

Initially:

 
Thread 0 → level 0
Thread 1 → level 0
Thread 2 → level 0
Thread 3 → level 0
 

A level of `0` means the thread is not trying to enter the critical section.

 

  `victim`

  
victim[L]
 

stores the thread that is currently the victim at level `L`.

When a thread reaches a level, it declares itself the victim:

  
victim[L].value = threadId;
 

The victim is the thread that may have to wait if another thread is competing at the same level.

 

  9. Filter Lock Algorithm

The thread passes through levels:

  
for (int L = 1; L < n; L++)
 

For every level, it performs two main operations.

 

  Step 1 — Move to the level

  
level[threadId].value = L;
 

For example:

 
Thread 2 reaches level 1:

level[2] = 1
 

Then:

 
Thread 2 reaches level 2:

level[2] = 2
 

 

  Step 2 — Become the victim

  
victim[L].value = threadId;
 

This means:

> "I am the victim at this level."

If another thread arrives at the same level after this thread, it can replace the current victim.

 

  10. Filter Lock Waiting Condition

A thread waits while both conditions are true:

 
1. Another thread is at the same level or higher.

AND

2. This thread is still the victim.
 

In code:

  
while (
    existsAnotherThreadAtOrAbove(threadId, L)
    &&
    victim[L].value == threadId
)
{
    Thread.yield();
}
 

This allows competing threads to pass through the levels.

 

  11. Filter Lock Example

Suppose there are three threads:

 
Thread 0
Thread 1
Thread 2
 

There are:

 
n - 1 = 2
 

levels:

 
Level 1
Level 2
 

Initially:

 
Thread 0 → level 0
Thread 1 → level 0
Thread 2 → level 0
 

Suppose Thread 0 enters level 1:

 
level[0] = 1
victim[1] = 0
 

Thread 1 then enters level 1:

 
level[1] = 1
victim[1] = 1
 

Thread 1 replaces Thread 0 as the victim.

Therefore Thread 1 may need to wait while Thread 0 continues.

Thread 0 can proceed to level 2:

 
level[0] = 2
victim[2] = 0
 

If no other thread is at level 2, Thread 0 enters the critical section.

 

  12. Filter Lock Unlock

When the thread leaves the critical section:

  
level[threadId].value = 0;
 

This tells all other threads:

> "I am no longer competing for the lock."

For example:

 
Before:

Thread 0 → level 2
Thread 1 → level 1
Thread 2 → level 0

After Thread 0 unlocks:

Thread 0 → level 0
Thread 1 → level 1
Thread 2 → level 0
 

Thread 1 can now continue.

 

  13. Bakery Lock vs Filter Lock

| Feature            | Bakery Lock    | Filter Lock         |
|        |     -- |       - |
| Main idea          | Take a number  | Pass through levels |
| Main array         | `label`        | `level`             |
| Additional array   | `flag`         | `victim`            |
| Number of levels   | Not applicable | `n - 1`             |
| Priority           | Lowest ticket  | Victim mechanism    |
| Tie breaking       | Thread ID      | Victim changes      |
| Unlock             | `label[i] = 0` | `level[i] = 0`      |
| Supports N threads | Yes            | Yes                 |
| Uses busy waiting  | Yes            | Yes                 |

 

  14. Important Concepts

  Mutual Exclusion

Only one thread should be inside the critical section at a time.

Both Bakery Lock and Filter Lock are designed to provide mutual exclusion.

 

  Busy Waiting

Both algorithms use busy waiting.

For example:

  
while (condition)
{
    Thread.yield();
}
 

The thread does not enter the critical section until the condition becomes false.

 

  Thread ID

Each thread needs a unique ID.

For example:

 
Thread 0
Thread 1
Thread 2
Thread 3
 

The ID is used by both algorithms to identify which thread is requesting the lock.

 

  15. Quick Exam Summary

  Bakery Lock

Remember:

 
1. Set flag to true
2. Get highest ticket + 1
3. Set flag to false
4. Wait for threads with higher priority
5. Enter critical section
6. Set label to 0 when finished
 

The priority rule is:

 
smallest (label, threadId) wins
 

 

Filter Lock

Remember:

 
1. Go through levels 1 to n-1
2. Set your level
3. Become the victim
4. Wait if another thread is at/above your level
   AND you are still the victim
5. Enter critical section
6. Set your level to 0 when finished
 

The main idea is:

 
Bakery → "Who has the smallest number?"

Filter → "Who gets filtered out at each level?"
 

 

16. Files

The project should contain:

 
Lock. 
VolatileInt. 
VolatileBoolean. 
BakeryLock. 
FilterLock. 
 

Both lock implementations follow the same interface:

  
Lock lock = new BakeryLock(n);
 

or:

  
Lock lock = new FilterLock(n);
 

This allows the rest of the program to use either algorithm through the same `Lock` interface.

 

17. Key Takeaway

The two algorithms solve the same fundamental problem:

**How can multiple threads safely enter a critical section without two threads entering at the same time?**

-Bakery Lock

Uses:

flag + ticket
 
and selects the thread with the smallest:

 
(ticket, threadId)
 

  Filter Lock

Uses:

 
level + victim
 

and makes threads pass through:

 
1 → 2 → ... → n-1
 

before they can enter the critical section.

Both algorithms achieve mutual exclusion without using  's built-in `synchronized` keyword or standard lock classes.
