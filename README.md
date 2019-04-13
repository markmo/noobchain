# noobchain

Implement a simple Blockchain and crypto coin from scratch.

A blockchain:

* Is made up of blocks that store data.
* Has a digital signature that chains your blocks together.
* Requires proof of work mining to validate new blocks.
* Can be checked to see if data in it is valid and hasn't been tampered with.

In crypto-currencies, coin ownership is transferred on the Blockchain as transactions, 
participants have an address which funds can be sent to and from.

For our NoobCoin, the public key will act as our address. Our private key is used to 
sign our transactions, so that nobody can spend our NoobCoins other than the owner of 
the private key.

On the bitcoin network nodes share their Blockchains and the longest valid chain is 
accepted by the network. To stop someone tampering with data in an old block by creating 
a whole new longer blockchain and presenting that to the network, proof-of-work is
employed.

We will require miners to do proof-of-work by trying different variable values in the 
block until its hash starts with a certain number of 0’s.

The `Block.mineBlock` method takes an int called difficulty. This is the number of 0’s 
that must be solved for. Low difficulty such as 1 or 2 can be solved nearly instantly on 
most computers. Around 4–6 is good for testing. Litecoin’s difficulty is around 442,592.

Because mining takes longer than the time typically for new blocks to be added to the
chain, a tampered blockchain will not be able to catch up with a valid chain (unless
teh attacker has vastly more computation speed than all other nodes in the network combined).

However, this last design constraint presents challenges in speed (transactions per second)
and energy efficiency for enterprise blockchain systems. On solutions, more to follow.


See [Kass, Medium](https://medium.com/programmers-blockchain/create-simple-blockchain-java-tutorial-from-scratch-6eeed3cb03fa)
for an excellent reference.