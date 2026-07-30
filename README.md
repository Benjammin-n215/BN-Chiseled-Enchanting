to do:

visual
- rather than scroll thru enchant options index, render full list as a scrollable element
- display roman numeral instead of raw number
- better UI

low prio
- fix server double checking on ui open?

---------------------
added:

(+) only show applicable enchantments
- create method in inscriberMenu that checks canApply() against whole list, returning compatible?
- fix crash

(+) allow enchanted items to be used

(+) get inscriber to retain name

(+) enchanting ingredients that can reduce cost of enchantment
- steep starting curve [x * x^(0.5i)]?
- using various magical ingredients (amethyst shard + echo shard + blaze powder) can flatten curve
- fix return items