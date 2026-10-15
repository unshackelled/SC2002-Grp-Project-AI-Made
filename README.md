# Restaurant Rush (SC2002 Group Assignment)

A turn-based restaurant management game played on the command line, in Java. You play the manager: each turn you decide what your limited staff do, and after 18 turns the game ends in Victory or Defeat.

Repository: https://github.com/unshackelled/SC2002-Grp-Project-AI-Made

| Folder | Stage | Adds | Git tag |
|---|---|---|---|
| [Stage1/](Stage1/) | Basic operation | Regular customers, menu, Waiter, Chef, Cashier, full turn sequence, both endings | `stage-1` |
| [Stage2/](Stage2/) | Customer behaviour | `VIPCustomer`, `CriticCustomer` | `stage-2` |
| [Stage3/](Stage3/) | New role and menu policy | `Host`, `ComboMeal`, `HappyHour` | `stage-3` |

Each folder can be compiled and played on its own. Each stage starts as a copy of the stage before it, so the files that differ between folders are exactly the stage-to-stage changes listed below.

---

## Build and run

You need JDK 11 or later (tested with OpenJDK 17). Nothing else is needed, and the game works offline.

```sh
cd Stage3                       # or Stage1 / Stage2
javac -d out -sourcepath src src/restaurantrush/Main.java
java -cp out restaurantrush.Main
```

Each stage also has a script that compiles and then runs it: `run.bat` on Windows, or `./run.sh` on macOS, Linux or Git Bash.

### Replaying a scripted run

Every stage has `tests/victory.txt` and `tests/defeat.txt`. Pass `--echo` so the scripted inputs show up in the output. Lines starting with `#` are comments and are skipped.

```sh
java -cp out restaurantrush.Main --echo < tests/victory.txt      # cmd / bash
Get-Content tests\victory.txt | java -cp out restaurantrush.Main --echo   # PowerShell
```

---

## How to play (base setting)

18 turns, 2 tables, one each of Waiter, Chef and Cashier (plus a Host in Stage 3). Customers arrive at the start of turns 1, 4, 7, 10, 13 and 16.

| Item | Price | Prep units |
|---|---|---|
| Salad | $9.00 | 1 |
| Burger | $15.00 | 1 |
| Pasta | $18.00 | 2 |
| Salad + Burger Combo (Stage 3) | $21.60 | 2 |
| Salad + Pasta Combo (Stage 3) | $24.30 | 3 |

**Objective:** after turn 18, at least 4 paid customers, revenue of at least $45, and an average satisfaction of at least 60 among paid customers.

**Each turn runs in this order:**
1. **Arrivals.** The scheduled customer joins the queue with satisfaction 100. The state is then displayed.
2. **Manager choices.** In Stage 3, you may first switch on Happy Hour, and then the Host may seat one customer. Next, the **Waiter** does one task: seat, take an order, serve a READY order, or wait. Then the **Chef** advances one order by one unit, or waits.
3. **Kitchen.** An order becomes READY when its last unit is done. A dish that became READY this turn can be served next turn at the earliest.
4. **Eating and payment.** A customer served on an earlier turn finishes eating and becomes READY_TO_PAY. The Cashier then automatically takes one payment, oldest first, from a customer who became READY_TO_PAY on an earlier turn.
5. **Waiting.** Every customer who has not been served yet loses satisfaction. At 0 they leave: their order is cancelled and their table freed.
6. **Result.** After turn 18 the game ends in Victory or Defeat.

If a choice is invalid (no such customer, no free table, the order isn't READY yet, and so on), the game explains why and asks again; the staff member's task isn't used up. If a staff member has nothing they could possibly do, the game makes them wait automatically, so you don't have to type `0` every turn.

### Documented rules (rules the brief leaves to the group)

| Rule | Our choice |
|---|---|
| **How customers choose food** | Deterministic rotation by arrival number. Customer *n* orders menu item `(n − 1) mod menuSize`, in menu order. The choice is shown when the order is taken. (`Customer.chooseItem`) |
| **Critic special behaviour** | The Critic ignores the rotation and always orders the **most expensive item on the menu**: Pasta in Stage 2, the Salad + Pasta Combo in Stage 3. That item is also the slowest to cook, which hurts more because Critics lose 12 per waiting turn. (`CriticCustomer.chooseItem`) |
| **How customers choose a combo** | The same rotation rule. The two combos are listed after the three single dishes, so in the base setting C4 orders the Salad + Burger Combo and C5 orders the Salad + Pasta Combo. No change to `Customer` was needed. |
| **Customer types in the base setting** | Stage 1: six Regulars. Stages 2–3: turn 4 is a **Critic** and turn 7 is a **VIP**; the rest are Regulars. |
| **Which table is used** | The Host or Waiter chooses *who* to seat. The customer gets the lowest-numbered free table (the tables are identical). |
| **Host timing** | The Host acts before the Waiter, so the Waiter can take the order of a customer the Host has just seated in the same turn. |
| **Happy Hour timing** | You are offered it at the start of each turn, before any staff act, until it has been used. It lasts for that turn and the next. The +15 recovery applies at the start of both active turns, to every customer still waiting for a table or for food. |
| **Price rounding** | All money is stored as integer cents. A percentage is rounded half-up to the nearest cent (`Money.percentOf`). A VIP's discount is applied first, then Happy Hour: a VIP's Burger during Happy Hour costs $15 × 0.9 × 0.8 = **$10.80**. |

---

## Design overview

Packages (the same in every stage):

| Package | Kind | Classes |
|---|---|---|
| `restaurantrush.ui` | Boundary | `ConsoleUI` (displays state, collects choices), `InputReader` (validated input), `InputEndedException` |
| `restaurantrush.control` | Control | `GameController` (turn sequence and rules), `GameConfig` (base setting), `ArrivalSchedule`, `CustomerFactory`, `Objective` |
| `restaurantrush.staff` | Entity (roles) | abstract `Staff` → `Waiter`, `Chef`, `Cashier`, `Host` (Stage 3) |
| `restaurantrush.model` | Entity | abstract `Customer` → `RegularCustomer`, `VIPCustomer`, `CriticCustomer`; abstract `MenuItem` → `Dish`, `ComboMeal`; `Menu`, `Order`, `Table`, `Restaurant`, `Scoreboard`, `HappyHour`, `ActionResult`, `Money`; enums `CustomerStatus`, `OrderStatus` |

### Key abstractions and responsibilities

| Abstraction | Responsibility |
|---|---|
| `GameController` | Runs the six turn steps in order, passes the manager's choices to the right staff member, and handles everything automatic (arrivals, eating, payment, waiting penalties, departures, Happy Hour recovery). |
| `ConsoleUI` | Shows the start-of-turn and end-of-turn summaries, asks for choices and shows `ActionResult` messages. It contains no game rules. |
| `Restaurant` | Keeps the tables, waiting queue, customers and orders consistent with each other. Holds the shared seating rule (`seatFromQueue`) that both the Waiter and the Host use. |
| `Customer` (abstract) | Holds satisfaction and the customer's progress through the service flow (the `CustomerStatus` transitions). Subclasses override `getSatisfactionLoss`, `chooseItem` and `calculateBill`. |
| `Staff` (abstract) | Enforces one task per turn and lets the manager choose Wait. Each role defines `hasPossibleTask` and `describeAvailableTasks`. |
| `MenuItem` (abstract) | Defines a price and a number of prep units. `ComboMeal` is made of two other `MenuItem`s. |
| `Order` | Locks in the bill when the order is taken. Tracks preparation progress and READY → SERVED → PAID (or CANCELLED). Enforces "served the turn after READY at the earliest". |
| `HappyHour` | The once-per-game pricing policy: whether it's active, how many turns remain, the 20% discount and the recovery amount. |
| `Scoreboard` / `Objective` | Running totals, and the three win conditions. |

### OO highlights

- **Encapsulation.** State changes go through methods that check them. For example, `Customer.seatAt` / `placeOrder` / `receiveFood` / `pay` each reject an invalid status, and `Table.assign` / `vacate` are package-private, so only `Customer` can change who sits at a table.
- **Polymorphism.** `GameController` calls `customer.loseSatisfactionForWaiting()`, `Waiter` calls `customer.chooseItem(menu)` and `customer.calculateBill(item)`, and neither ever checks the concrete type. The start-of-turn "staff tasks available" lines come from looping over `List<Staff>` and calling `describeAvailableTasks`.
- **Composition.** `Restaurant` owns its `Table`s, queue and `Order`s. `GameController` owns the staff and the `Restaurant`. A `ComboMeal` is made up of two `MenuItem`s.
- **OCP.** Adding VIP and Critic needed no change to `GameController`, `Waiter`, `Chef` or `Cashier`: only two new subclasses and two registration lines in `GameConfig`. Adding combos needed no change to `Order`, `Chef` or `Customer`.
- **LSP.** A `ComboMeal` works anywhere a `MenuItem` is expected, and any `Customer` subclass goes through the same order and service flow.
- **SRP.** Rules live in control and entity classes, input and output live in `ConsoleUI` / `InputReader`, and money formatting lives in `Money`.

---

## Stage changes

The compare links work once the stage commits and tags have been pushed.

### Stage 1 → Stage 2 ([compare](https://github.com/unshackelled/SC2002-Grp-Project-AI-Made/compare/stage-1...stage-2))

| File | Change | Reason |
|---|---|---|
| `model/VIPCustomer.java` | **New** | Loses 5 per waiting turn, and overrides `calculateBill` to give 10% off. |
| `model/CriticCustomer.java` | **New** | Loses 12 per waiting turn, and overrides `chooseItem` to pick the most expensive item. |
| `model/Money.java` | Changed: added `percentOf` | Percentage discounts now need rounding to the nearest cent. |
| `control/GameConfig.java` | Changed: two registration lines | Turns 4 and 7 now create a Critic and a VIP, keeping six arrivals in total. |
| `ui/ConsoleUI.java` | Changed: banner and help text only | Shows the stage number and explains the customer types. |

`GameController`, every `Staff` class, `Order` and `Customer` are **unchanged**. The new behaviour is reached through methods `Customer` already declared.

### Stage 2 → Stage 3 ([compare](https://github.com/unshackelled/SC2002-Grp-Project-AI-Made/compare/stage-2...stage-3))

| File | Change | Reason |
|---|---|---|
| `staff/Host.java` | **New** | A second staff member who can seat customers. It reuses `Restaurant.seatFromQueue`, so the seating rule isn't duplicated. |
| `model/ComboMeal.java` | **New** | A `MenuItem` made of two `MenuItem`s: prep units are the sum, and the price is 90% of the combined price. |
| `model/HappyHour.java` | **New** | The pricing policy: active-turn countdown, the 20% discount and the recovery amount. |
| `model/Customer.java` | Changed: added `recover(int)` | Happy Hour must raise satisfaction, capped at 100. Satisfaction is private, so `Customer` needs a method for this. |
| `staff/Waiter.java` | Changed: `takeOrder` takes a `HappyHour` | The price is locked when the order is taken, so the Happy Hour discount is applied there, after the customer's own discount. |
| `control/GameController.java` | Changed | Adds the Host to the staff list, Happy Hour activation and validation (once per game, at turn start), recovery at the start of each active turn, and the turn countdown. |
| `control/GameConfig.java` | Changed: menu setup | Adds the two combos after the single dishes. |
| `ui/ConsoleUI.java` | Changed | Adds the Happy Hour prompt and status line (remaining turns), the Host phase and the new help text. |

No earlier game rule was rewritten. The Stage 1 and Stage 2 scripts still win and lose in their own stage folders.

---

## Test notes (reproducible runs)

| Stage | Script | Result | Paid | Revenue | Avg satisfaction | Unhappy departures |
|---|---|---|---|---|---|---|
| 1 | `Stage1/tests/victory.txt` | **VICTORY** | 5 | $66.00 | 79.2 | 0 |
| 1 | `Stage1/tests/defeat.txt` | **DEFEAT** | 0 | $0.00 | N/A | 2 |
| 2 | `Stage2/tests/victory.txt` | **VICTORY** | 5 | $67.20 | 76.0 | 0 |
| 2 | `Stage2/tests/defeat.txt` | **DEFEAT** | 0 | $0.00 | N/A | 2 |
| 3 | `Stage3/tests/victory.txt` | **VICTORY** | 5 | $92.16 | 81.0 | 0 |
| 3 | `Stage3/tests/defeat.txt` | **DEFEAT** | 0 | $0.00 | N/A | 2 |

Points to look for in the runs:
- **Stage 1, turn 10.** The Waiter is the bottleneck. Serving C3's READY Pasta first keeps C3 happy, but the newly arrived C4 waits a turn.
- **Stage 1, turn 3.** An invalid "serve order #2" is refused with an explanation and the Waiter chooses again.
- **Stage 2, turn 5.** The Critic orders Pasta instead of the Burger the rotation would give.
- **Stage 2, turn 7.** The impatient Critic is served before the patient VIP is seated.
- **Stage 2, turn 9.** The VIP's Pasta is billed at $16.20.
- **Stage 3, turn 7.** Switching on Happy Hour lifts the waiting Critic from 64 to 79.
- **Stage 3, turn 8.** The VIP's order is billed at $18 × 0.9 × 0.8 = $12.96.
- **Stage 3, turn 13.** The Waiter takes C5's 3-unit combo order before serving C4. Otherwise C5 could not finish paying by turn 18.
- **Stage 3, turn 16.** Seating C6 is refused because both tables are taken.
- **Defeat runs.** The Waiter never works. Nobody pays, and the Critic is the first to walk out in Stages 2–3.
- **The turn-16 customer** can never pay within 18 turns, which the brief says is intended.

---

## Video timestamps

| # | Segment | Timestamp |
|---|---|---|
| 1 | Game launching from the submitted project | _TBD_ |
| 2 | Meaningful manager choices, including a decision where staff are constrained | _TBD_ |
| 3 | A dish going READY → SERVED → payment | _TBD_ |
| 4 | A VIP or Critic behaviour | _TBD_ |
| 5 | A Stage 3 feature | _TBD_ |
| 6 | One victory and one defeat | _TBD_ |
| 7 | Stage tag comparisons and the diagrams | _TBD_ |
