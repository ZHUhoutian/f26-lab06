# Contract Worksheet

One section per milestone. Fill each one in as you go, in order. Write each
prediction before you run anything. That is the part a TA asks about.

Keep it short and specific. Point at methods, call sites, and error text.

---

## Milestone 1: The notes overload

### Prediction (write this before you run the build, and you can deliberate with your agent)

**Will the consumer, untouched, still compile and pass?** Yes.

**Why.** What does the compiler do with the consumer's existing call sites once
the new overload exists?
Java resolves overloads at compile time by number and types of arguments. The consumer's 4-argument calls still match only the original signature, so the new 5-argument overload is invisible to them.
### What happened

**The result.** What the build printed for each module.

`mvn -B clean test` after adding `createBooking(String, long, long, String, String notes)`
to `BookingApi` / `InMemoryBookingService` and `getNotes()` to `Booking`
(`consumer/` untouched):

```
[INFO] Building lab06-api 1.0.0                                           [2/3]
[INFO] Compiling 4 source files with javac [debug deprecation release 21] to target/classes
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
[INFO] Building lab06-consumer 1.0.0                                      [3/3]
[INFO] Compiling 1 source file with javac [debug deprecation release 21] to target/classes
[INFO] Compiling 1 source file with javac [debug deprecation release 21] to target/test-classes
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0
[INFO] lab06-booking-parent ............................... SUCCESS
[INFO] lab06-api .......................................... SUCCESS
[INFO] lab06-consumer ..................................... SUCCESS
[INFO] BUILD SUCCESS
```

Both modules green, no warnings. The consumer recompiled against the new API
and all 7 of its tests passed.

**If your prediction was wrong,** say what you missed.

**Is an additive change always safe in Java?** One case where adding something
to an API still breaks a caller, if you can name one.
Adding an abstract method breaks anyone who implements BookingApi. A same-arity overload can make a null argument ambiguous.
---

## Milestone 2: The request object

### Prediction (write this before you run the build)

**Will the untouched consumer still compile and pass?** Yes or no, and if no,
which module goes red and whether at compile time or test time.
No. `lab06-consumer` goes red at compile time. `FrontDesk` holds a
`BookingApi`, and once the positional overloads are removed the interface no
longer declares `createBooking(String, long, long, String)`, so `javac` reports
`cannot find symbol`. Because compilation fails, the consumer's 7 tests never
run at all.

**Where.** Name the call sites you expect to be affected, if any.
`FrontDesk.bookWalkIn` (`api.createBooking(roomId, startMinute, endMinute, null)`)
and `FrontDesk.joinWaitlist` (`api.createBooking(roomId, startMinute, endMinute, guestName)`).
The `listBookings` and `cancelBooking` calls are unaffected, since those
signatures do not change.

**What about the tests in `api/`, after you update them?** And whether their
result is evidence about the consumer.
They should pass. All five are rewritten to call `createBooking(BookingRequest)`,
and if the implementation keeps the same behavior they check the same things.
But a green `api/` is not evidence about the consumer: those tests only
exercise code we changed in step with them, so they cannot see a caller we do
not control. Only the consumer's own build can catch this break.

### Step 1: after the fold

**What the build printed.** Paste it for each module, including file and
line for anything that failed.

Change: added `BookingRequest` (`BookingRequest.of(roomId, start, end)` plus
`withWaitlistKey(..)` / `withNotes(..)`), replaced both positional
`createBooking` overloads in `BookingApi` and `InMemoryBookingService` with
`createBooking(BookingRequest)`, and rewrote all five `api/` tests to the new
call. `consumer/` untouched. Output of `mvn -B clean test`:

`lab06-api`:

```
[INFO] Building lab06-api 1.0.0                                           [2/3]
[INFO] Compiling 5 source files with javac [debug deprecation release 21] to target/classes
[INFO] Compiling 1 source file with javac [debug deprecation release 21] to target/test-classes
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
```

`lab06-consumer`:

```
[INFO] Building lab06-consumer 1.0.0                                      [3/3]
[INFO] Compiling 1 source file with javac [debug deprecation release 21] to target/classes
[ERROR] COMPILATION ERROR :
[ERROR] .../consumer/src/main/java/edu/cmu/cs214/frontdesk/FrontDesk.java:[27,19] method createBooking in interface edu.cmu.cs214.booking.BookingApi cannot be applied to given types;
[ERROR]   required: edu.cmu.cs214.booking.BookingRequest
[ERROR]   found:    java.lang.String,long,long,<nulltype>
[ERROR]   reason: actual and formal argument lists differ in length
[ERROR] .../consumer/src/main/java/edu/cmu/cs214/frontdesk/FrontDesk.java:[33,19] method createBooking in interface edu.cmu.cs214.booking.BookingApi cannot be applied to given types;
[ERROR]   required: edu.cmu.cs214.booking.BookingRequest
[ERROR]   found:    java.lang.String,long,long,java.lang.String
[ERROR]   reason: actual and formal argument lists differ in length
```

Reactor summary:

```
[INFO] lab06-booking-parent ............................... SUCCESS
[INFO] lab06-api .......................................... SUCCESS
[INFO] lab06-consumer ..................................... FAILURE
[INFO] BUILD FAILURE
[ERROR] Failed to execute goal org.apache.maven.plugins:maven-compiler-plugin:3.13.0:compile (default-compile) on project lab06-consumer: Compilation failure
```

Note vs. prediction: the error text is `cannot be applied to given types`,
not `cannot find symbol`. A method named `createBooking` still exists on the
interface, just with a different parameter list, so `javac` finds the name and
rejects the arguments.

**Which module's tests ran, and which did not.** And what that tells you about
who can detect a contract break.

The `api/` tests ran (5 run, 0 failures). The consumer's tests did not run:
the build stopped at `lab06-consumer`'s `compile` goal, so `FrontDeskTest`
never reached the `test` phase (no `Tests run` line for it).

### Step 2: the deprecation path

**What you added.** The signatures that came back, and what they delegate to.

Both old signatures came back on `BookingApi` as `@Deprecated` `default`
methods, each with a `@deprecated` javadoc naming the replacement:

```java
@Deprecated
default Booking createBooking(String roomId, long startMinute, long endMinute,
                              String waitlistKey)
// -> createBooking(BookingRequest.of(roomId, startMinute, endMinute)
//        .withWaitlistKey(waitlistKey))

@Deprecated
default Booking createBooking(String roomId, long startMinute, long endMinute,
                              String waitlistKey, String notes)
// -> createBooking(BookingRequest.of(roomId, startMinute, endMinute)
//        .withWaitlistKey(waitlistKey).withNotes(notes))
```

Because they are `default` methods on the interface, the behavior lives in one
place (`createBooking(BookingRequest)`), and an implementation of `BookingApi`
only has to implement the new method. `InMemoryBookingService` was not changed
in this step.

**The warnings.** Paste one deprecation warning line from the build log (from
a `mvn -B clean test` run, since a rerun with nothing to compile prints none).

```
[WARNING] /home/houtian/Desktop/repos/17514/f26-lab06/consumer/src/main/java/edu/cmu/cs214/frontdesk/FrontDesk.java:[27,19] createBooking(java.lang.String,long,long,java.lang.String) in edu.cmu.cs214.booking.BookingApi has been deprecated
```

(A second identical warning at `FrontDesk.java:[33,19]`.) What changed from
step 1, for `lab06-consumer`:

```
[INFO] Compiling 1 source file with javac [debug deprecation release 21] to target/classes
[WARNING] .../FrontDesk.java:[27,19] createBooking(java.lang.String,long,long,java.lang.String) in edu.cmu.cs214.booking.BookingApi has been deprecated
[WARNING] .../FrontDesk.java:[33,19] createBooking(java.lang.String,long,long,java.lang.String) in edu.cmu.cs214.booking.BookingApi has been deprecated
[INFO] Compiling 1 source file with javac [debug deprecation release 21] to target/test-classes
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0
...
[INFO] lab06-booking-parent ............................... SUCCESS
[INFO] lab06-api .......................................... SUCCESS
[INFO] lab06-consumer ..................................... SUCCESS
[INFO] BUILD SUCCESS
```

The two compile errors became two warnings, the consumer's 7 tests ran and
passed, and the build is green. `lab06-api` is unchanged (5 tests, 0 failures,
no warnings, since its tests already use the new call).

**What the deprecation path resolves.** Who can now build that could not build
during step 1, and who is on which schedule.

**What the warnings accomplish that a README note would not.** Be concrete
about where the warning shows up and who sees it without looking for it.

---

## Milestone 3: The misuse critique

Not coded. One misuse, one redesign, one cost. Discuss it with your TA.

### The misuse

**What is easy to get wrong.** One specific thing about the API surface.

**The call site.** File and line in `consumer/`, with the call. Show the
code that a reader cannot understand without opening the javadoc, or that a
caller could get wrong with the compiler still happy.

**What goes wrong when it happens.** Silent bad behavior, wrong data, a crash
somewhere far away?

### The redesign

**The proposal.** Types, enums, factories, or whatever you are proposing. Show
the new signature and the new call site.

**Why the mistake is now hard or impossible to make.** Point at the mechanism,
such as the compiler, a validating constructor, or an exhaustive switch.

### One tradeoff

**What it costs.** Something real, such as caller ceremony, migration burden
against the deprecation path you just built, or more types for a newcomer to
learn. "No real downside" does not count.

**When the price is worth paying.** A condition under which it is.
