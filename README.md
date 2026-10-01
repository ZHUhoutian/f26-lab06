# Lab 6 Starter: Booking API and a Consumer You Do Not Own

Two Maven modules in one repo. `api/` is a room booking API you maintain.
`consumer/` is a walk-in front desk app built on top of it by another team.

You change the API in `api/`. The front desk team is not in the room, does not
answer your messages (on time, at least), and their test suite runs in the same
build as yours. Their suite is the gate. It decides whether a change you called
harmless was actually harmless.

## Module map

- `api/` (package `edu.cmu.cs214.booking`)
  - `BookingApi.java`: the interface, and its javadoc is the written contract.
    Read this one closely. Every milestone argues about what it promises.
  - `Booking.java`, `BookingStatus.java`: the data the API hands back.
  - `InMemoryBookingService.java`: the working implementation.
  - Tests under `src/test/java`: the producer's own suite. Note what it does
    and does not check.
- `consumer/` (package `edu.cmu.cs214.frontdesk`)
  - `FrontDesk.java`: books walk-ins, queues guests on the waitlist, prints a
    room's schedule, cancels two different ways.
  - `FrontDeskTest.java`: the contract gate.

Do not edit anything under `consumer/`. You may read it and run it.

## Build and test

```
mvn -B test
```

From this directory. Maven builds `api` first, then compiles and tests
`consumer` against it, in the same command.

## Where things are

- Setup: `SETUP.md`
- Your worksheet: `CONTRACT.md`, filled in as you go, one section per milestone
- CI: `.github/workflows/ci.yml`, the same command on every push. GitHub
  disables workflows on a fresh fork, so enable them from the Actions tab if
  you want it running.

See the Lab 6 handout on the course page for the three milestones you show a TA.

## Tools used

Claude Code (VS Code extension) with Claude Opus 5.5 (`claude-opus-5-5`).
