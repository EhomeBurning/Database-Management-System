#include "bnode_inner.h"
#include <vector>

using namespace std;

VALUETYPE Bnode_inner::merge(Bnode_inner* rhs, int parent_idx) {
    assert(rhs->parent == parent); // can only merge siblings
    // assert(rhs->num_values > 0); /////// commented this line
    
    assert( num_values + rhs->num_values < BTREE_FANOUT - 1);
    
    // store values/children of this node in this intermidiate array
    vector<VALUETYPE> all_values(values, values + num_values);
    vector<Bnode*> all_children(children, children + num_children);
    
    VALUETYPE parent_value = parent->get( parent_idx );
    
    // insert parent_value into all_values
    all_values.push_back(parent_value);
    
    // insert rhs values to all_values
    int insert_value;
    for ( int i = 0; i < rhs->getNumValues(); ++i){
        insert_value = rhs->get(i);
        all_values.push_back(insert_value);
    }
    
    // insert rhs children node to all_children
    Bnode* insert_node;
    for ( int i = 0; i < rhs->getNumChildren(); ++i){
        insert_node = rhs->getChild(i);
        all_children.push_back(insert_node);
    }

    clear();
    rhs->clear();
    
    for ( int i = 0; i < all_values.size(); ++i ){
        insert(all_values[i]);
    }
    
    for ( int i = 0,idx = 0; i < all_children.size(); ++i, ++idx){
        insert(all_children[i],idx);
        all_children[i] -> parent = this;
    }
    
    rhs->parent = nullptr; // may cause memory leak here
    // delete rhs; // do this instead ?
    
    // TODO: Implement this

    return parent_value;
}


VALUETYPE Bnode_inner::redistribute(Bnode_inner* rhs, int parent_idx) {
    assert(rhs->parent == parent); // inner node redistribution should only happen with siblings
    assert(parent_idx >= 0);
    assert(parent_idx < parent->getNumValues());

    // TODO: Implement this
    
    int total_num = num_values + rhs->num_values + 1;
    
    // store values/children of this node in this intermidiate array
    vector<VALUETYPE> all_values(values, values + num_values);
    vector<Bnode*> all_children(children, children + num_children);
    
    VALUETYPE parent_value = parent->get( parent_idx );
    
    // insert parent_value into all_values
    all_values.push_back(parent_value);
    
    // insert rhs values to all_values
    int insert_value;
    for ( int i = 0; i < rhs->getNumValues(); ++i){
        insert_value = rhs->get(i);
        all_values.push_back(insert_value);
    }
    
    // insert rhs children node to all_children
    Bnode* insert_node;
    for ( int i = 0; i < rhs->getNumChildren(); ++i){
        insert_node = rhs->getChild(i);
        all_children.push_back(insert_node);
    }
    
//    VALUETYPE parent_value = parent->get( parent_idx );
//    
//    vector<VALUETYPE> all_values(values, values + num_values);
//    all_values.push_back(parent_value);
//    
//    int insert_value;
//    for ( int i = 0; i < rhs->getNumValues(); ++i){
//        insert_value = rhs->get(i);
//        all_values.push_back(insert_value);
//    }
    
//    vector<VALUETYPE> all_values(total_num);
//    vector<Bnode*> all_childeren(total_num + 1);
//    
//    for ( int i = 0; i < total_num; ++i){
//        if ( i < num_values)
//            all_values[i] = get(i);
//        else if ( i == num_values )
//            all_values[i] = parent_value;
//        else
//            all_values[i] = rhs->get(i);
//    }
    
    assert( all_values.size() == total_num );
    assert( all_children.size() == total_num + 1);
    
    clear();
    rhs->clear();
    
    VALUETYPE newParent_value = -1;
    
    // redistribute values from all_values
    for ( int i = 0; i < total_num; ++i){
        if ( i < ( total_num - 1) / 2 )
            insert(all_values[i]);
        else if ( i == ( total_num - 1) / 2 )
            newParent_value = all_values[i];
        else
            rhs->insert(all_values[i]);
    }
    
    // redistribute children from all_children
    int idx = 0;
    for ( int i = 0; i < total_num + 1; ++i){
        
        if ( i < ( total_num + 1) / 2 ){
            insert(all_children[i], i);
            all_children[i]->parent = this;
        }
        else{
            rhs->insert(all_children[i], idx );
            all_children[i]->parent = rhs;
            idx++;
        }
    }
    
    return newParent_value;
}


Bnode_inner* Bnode_inner::split(VALUETYPE& output_val, VALUETYPE insert_value, Bnode* insert_node) {
    assert(num_values == BTREE_FANOUT-1); // only split when it's full!

    // Populate an intermediate array with all the values/children before splitting - makes this simpler
    vector<VALUETYPE> all_values(values, values + num_values);
    vector<Bnode*> all_children(children, children + num_children);

    // Insert the value that created the split
    int ins_idx = find_value_gt(insert_value);
    all_values.insert(all_values.begin()+ins_idx, insert_value);
    all_children.insert(all_children.begin()+ins_idx+1, insert_node);

    // Do the actual split into another node
    Bnode_inner* split_node = new Bnode_inner;

    assert(all_values.size() == BTREE_FANOUT);
    assert(all_children.size() == BTREE_FANOUT+1);

    // Give the first BTREE_FANOUT/2 values to this bnode
    clear();
    for (int i = 0; i < BTREE_FANOUT/2; ++i)
        insert(all_values[i]);
    for (int i = 0, idx = 0; i < (BTREE_FANOUT/2) + 1; ++i, ++idx) {
        insert(all_children[i], idx);
        all_children[i] -> parent = this;
    }

    // Middle value should be pushed to parent
    output_val = all_values[BTREE_FANOUT/2];

    // Give the last BTREE/2 values to the new bnode
    for (int i = (BTREE_FANOUT/2) + 1; i < all_values.size(); ++i)
        split_node->insert(all_values[i]);
    for (int i = (BTREE_FANOUT/2) + 1, idx = 0; i < all_children.size(); ++i, ++idx) {
        split_node->insert(all_children[i], idx);
        all_children[i] -> parent = split_node;
    }

    // I like to do the asserts :)
    assert(num_values == BTREE_FANOUT/2);
    assert(num_children == num_values+1);
    assert(split_node->getNumValues() == BTREE_FANOUT/2);
    assert(split_node->getNumChildren() == num_values + 1);

    split_node->parent = parent; // they are siblings

    return split_node;
}
