#include <iostream>

#include "bnode_leaf.h"

using namespace std;

Bnode_leaf::~Bnode_leaf() {
    // Remember to deallocate memory!!

//    if ( next && prev ){      //connecting the next/prev pointer?
//        prev->next = next;
//        next->prev = prev;
//    }

//    if (num_values){
//        for (int i = 0; i < num_values; ++i) 
//        { 
//            delete values[i]; 
//        }
//    }
    // do we need to reset num_values ? or automatically deallocated ?;
}

VALUETYPE Bnode_leaf::merge(Bnode_leaf* rhs) {
    assert(num_values + rhs->getNumValues() < BTREE_LEAF_SIZE); // content in two nodes will not fully fill one node
    // assert(rhs->num_values > 0); // !!!!!! commented this line
    VALUETYPE retVal;
    
    if(rhs->num_values >0) {
        retVal = rhs->get(0);
    }
    
    else
        retVal = 0;

    Bnode_leaf* save = next; // a pointer save to save next pointer content 
    next = next->next; // current node point to the next next node
    if (next) next->prev = this; // exist next, then next next node point to current node

    for (int i = 0; i < rhs->getNumValues(); ++i) //insert data from rhs node to current node 
        insert(rhs->getData(i));

    rhs->clear(); // delete the content of rhs node 
    return retVal;
}

VALUETYPE Bnode_leaf::redistribute(Bnode_leaf* rhs) {
    // TODO: Implement this
    
    // rhs means right side of two nodes
    assert( num_values == BTREE_LEAF_SIZE/2 - 1 || rhs->getNumValues() == BTREE_LEAF_SIZE/2 - 1 );
    assert( num_values > BTREE_LEAF_SIZE/2 || rhs->getNumValues() > BTREE_LEAF_SIZE/2 );

    
    
    int target = (rhs->getNumValues() + num_values)/ 2;
    int rhs_target = rhs->getNumValues() + num_values - target;
    
    while (num_values > target){  // redistribute from this to rhs
        rhs->insert( get(num_values-1) );
        num_values--;
    }
    
    VALUETYPE rmVal;
    
    while ( rhs->getNumValues() > rhs_target){  // redistribute from rhs to this
        insert(rhs->get(0));

        rmVal = rhs->get(0);
        rhs->remove(rmVal);
    }
    
    // assert this and rhs after redistribution
    assert( num_values == target && rhs->getNumValues() == rhs_target );
    
    return rhs->get(0);

}

Bnode_leaf* Bnode_leaf::split(VALUETYPE insert_value) {
    assert(num_values == BTREE_LEAF_SIZE);
    // TODO: Implement this

    // create rhs node
    Bnode_leaf* rhs = new Bnode_leaf();
    
    // setting pointer (tricky)
    Bnode_leaf* save = next;
    
    next = rhs;
    rhs->prev = this;
    rhs->next = save;
    if (save){
        save->prev = rhs;
    }
    
    // splitting this node
    for( int i = BTREE_LEAF_SIZE/2; i < BTREE_LEAF_SIZE; ++i){
        rhs->insert(get(i));
    }
    
    VALUETYPE rmVal;
    for( int i = BTREE_LEAF_SIZE/2; i < BTREE_LEAF_SIZE; ++i){
        rmVal = get(BTREE_LEAF_SIZE/2);
        remove(rmVal);
    }
    
//    cout << " check what we have before insertion " << endl;
//    cout << "num of values in this node: "<< this->getNumValues() << endl;
//    cout << "num of values in rhs node: "<< rhs->getNumValues() <<endl;
//    
//    cout << "value(s) in this node: " <<this->get(0) << endl;
//    cout << "value(s) in rhs node: " << rhs->get(0) << endl;
//    

    //insert the remaining one
    if ( insert_value > rhs->get(0) )
    {
//        cout << " what we inserted is: " << insert_value <<endl;
        
        rhs->insert(insert_value);
        
//        cout << "number of values in rhs now :" << rhs->getNumValues() <<endl;
//        
//        cout << "now we have in rhs"<<endl;
//        cout << rhs->get(0) << endl;
//        cout << rhs->get(1) << endl;// for debug
    }
    
    else
        insert(insert_value);

//    if(parent){
//        rhs->parent = parent;
//        rhs_idx = parent->insert(cpval);
//        parent->insert(rhs,rhs_idx+1);
//    }
//
//    else {
//        Bnode_inner* new_parent = new Bnode_inner();
//        parent = new_parent;
//        rhs->parent = new_parent;
//
//        parent->insert(cpVal);
//        parent->insert(this, 0);
//        parent->insert(rhs, 1);
//    }

    return rhs;
}


