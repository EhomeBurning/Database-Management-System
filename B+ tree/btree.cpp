#include <iostream>

#include "btree.h"
#include "bnode.h"
#include "bnode_inner.h"
#include "bnode_leaf.h"

#include <cassert>

using namespace std;

const int LEAF_ORDER = BTREE_LEAF_SIZE/2;
const int INNER_ORDER = (BTREE_FANOUT-1)/2;

Btree::Btree() : root(new Bnode_leaf), size(0) {
    // Fill in here if needed
}

Btree::~Btree() {
    // Don't forget to deallocate memory
}

bool Btree::insert(VALUETYPE value) {
    // TODO: Implement this
    if (search(value)){
        // cout << " this value already exists !" << endl; // comment this line
        return false;
    }
    
    else{
        size ++;
        // the tree is empty
        
        if (size <= BTREE_LEAF_SIZE ) {
            
            Bnode_leaf* node = dynamic_cast<Bnode_leaf*>(root);
            assert(node);
            
            node->insert(value);
            
//            Bnode_leaf* new_node = new Bnode_leaf();
//            new_node->insert(value);
//            root = dynamic_cast<Bnode_leaf*>(new_node);
            
            return true;
        }
        
        
        Bnode_leaf* leaf = find_leaf_node(root, value);
        
        // the target leaf node is not full
        
        if ( leaf->getNumValues() < BTREE_LEAF_SIZE ){
            leaf->insert(value);
            return true;
            }
        
        else{
            split_leaf_insert(leaf, value);
            return true;
        }
        
        
//        if ( size <= BTREE_LEAF_SIZE ){
//            Bnode_leaf* current = dynamic_cast<Bnode_leaf*>(root);
//            assert(current);  // check if root node is a leaf node when size <= leaf size
//            
//            if (size < BTREE_LEAF_SIZE){
//                current->insert(value);
//                return true;
//            }
//            
//            else{
//                split_leaf_insert(current, value);
//                return true;
//            }
//            
//           else
//               Bnode_leaf* new_leaf = current->split(value);
//               Bnode_inner* new_parent = new Bnode_inner();
//               VALUETYPE new_parent_value =
//        }
//
//        cout << " We should not get here when insert values " << endl;
        return true;
    }
}

//////

bool Btree::remove(VALUETYPE value) {
    // TODO: Implement this
    
    // case(can not find the value)
    if(!search(value)){
        // cout<<"The value you wannna remove does not exist!"<<endl; ///// comment this line
        return false;
    }
    
    // assume the value is successfully removed
    size--;
    
    // remove the value
    // merge or redis?
    Bnode_leaf* leaf = find_leaf_node(root, value);
    
    leaf->remove(value);
    
    int num_val = leaf->getNumValues();
    
    Bnode_leaf* lhs = new Bnode_leaf();
    Bnode_leaf* rhs = new Bnode_leaf();
    
//    lhs = leaf->prev;
//    rhs = leaf->next;
    
    
//    int lhs_num_val = lhs->getNumValues();
//    int rhs_num_val = rhs->getNumValues();
    
    int lhs_num_val = -1;
    int rhs_num_val = -1;
    
//    Bnode_inner* lhs_par = lhs->parent;
//    Bnode_inner* rhs_par = rhs->parent;
    Bnode_inner* lhs_par;
    Bnode_inner* rhs_par;
    
    int index = -1;
    
    // corner case
    if(root == leaf){
        return true;
    }
    
    
    // delete directly
    if(num_val >= LEAF_ORDER){
        return true;
    }
    
    
    // can not delete directly
    // left side
    else if( leaf->prev == nullptr){
        lhs = leaf;
        rhs = leaf -> next;
        
        rhs_num_val = rhs->getNumValues();
        
        if(rhs_num_val > LEAF_ORDER) index = 1;
        else index = 0;
    }
    
    // right side
    else if(leaf->next == nullptr){
        rhs = leaf;
        lhs = leaf->prev;
        
        lhs_num_val = lhs->getNumValues();
        
        if(lhs_num_val > LEAF_ORDER) index = 1;
        else index = 0;
    }
    
    // in the middle
    else{
        if(leaf->next->getNumValues() > LEAF_ORDER){
            index = 1;
            lhs = leaf;
            rhs = leaf->next;
        }
        else if(leaf->prev->getNumValues() > LEAF_ORDER) {
            index = 1;
            lhs = leaf->prev;
            rhs = leaf;
        }
        
        //merge
        else{
            index = 0;
            lhs = leaf;
            rhs = leaf->next;
        }
    }
    
    assert(index == 0 || index == 1);
    
    
    lhs_par = lhs->parent;
    rhs_par = rhs->parent;
    
    // redistribution
    if(index){
        
        // same parent case
        if(lhs_par == rhs_par){
            
            
            int NewParent_val = lhs->redistribute(rhs);
            int index = lhs_par->find_child(lhs);
            rhs_par->replace_value(NewParent_val, index);
            return true;
        }
        
        // not the same parent
        else{
            
            int out_val = lhs->redistribute(rhs);
            get_common_ancestor(out_val, lhs, rhs);
            return true;
        }
    }
    
    // merge
    else{
        
        int del_val;
        int del_ind;
        
        if(lhs_par == rhs_par){
            
            if (rhs->getNumValues() == 0){
                del_val = value;
                lhs->merge(rhs);  ///////!!! we changed here
            }
            else
                del_val = lhs->merge(rhs);   // change here
            
            del_ind = rhs_par->find_value_gt(del_val)-1; // !!!! changed from lhs to rhs
            inner_node_delete(rhs_par, del_ind);
            return true;
           
        }
        else{
            
//            del_val = lhs->merge(rhs);
//            int del_ind = rhs_par->find_value_gt(del_val);
            del_ind = -1;
            del_val = rhs_par->get(0);
            get_common_ancestor(del_val, lhs, rhs);
            lhs->merge(rhs);  ///////!!! we changed here
            inner_node_delete(rhs_par, del_ind);
            
        }
        

    }
    
    return true;
}

//////

vector<Data*> Btree::search_range(VALUETYPE begin, VALUETYPE end) {
    std::vector<Data*> returnValues;
    
    assert(root);
    Bnode* current = root;
    
    // Have not reached a leaf node yet
    Bnode_inner* inner = dynamic_cast<Bnode_inner*>(current);
    // A dynamic cast <T> will return a nullptr if the given input is not polymorphically a T
    //                    will return a upcasted pointer to a T* if given input is polymorphically a T
    while (inner) {
        int find_index = inner->find_value_gt(begin);
        current = inner->getChild(find_index);
        inner = dynamic_cast<Bnode_inner*>(current);
    }
    
    // Found a leaf node
    Bnode_leaf* leaf = dynamic_cast<Bnode_leaf*>(current);
    
    assert(leaf);
    
    int traversed_value = -1;
    
    while ( traversed_value < end && leaf ){
        
        // traverse current leaf node
        for (int i = 0; i < leaf->getNumValues(); ++i) {
            
            if ( leaf->get(i) < begin )
                traversed_value = leaf->get(i);
            
            else if ( leaf->get(i) >= begin && leaf->get(i) <= end ){
                returnValues.push_back(leaf->getData(i)) ;
                traversed_value = leaf->get(i);
            }
            
            else {  // passed the possible location
                assert(leaf->get(i) > end);
                traversed_value = leaf->get(i);
                return returnValues;
            }
        }
        leaf = leaf->next;
    }
    return returnValues;
}

// additional implementation


Bnode_leaf* Btree::find_leaf_node( Bnode* root, VALUETYPE value){
    
    assert(root);
    Bnode* current = root;
    Bnode_inner* inner = dynamic_cast<Bnode_inner*>(current);
    
    while(inner){
        int find_value = inner->find_value_gt(value);
        current = inner->getChild(find_value);
        inner = dynamic_cast<Bnode_inner*>(current);
    }
    Bnode_leaf* leaf = dynamic_cast<Bnode_leaf*>(current);
    assert(leaf);
    
    return leaf;
}

void Btree::inner_node_insert(Bnode_inner* current, VALUETYPE insert_val, Bnode* child){
    
    assert(root);
    
    int num_val = current->getNumValues();
    
    if (num_val < BTREE_FANOUT -1){
        
        int index = current-> find_value_gt(insert_val);
        current->insert(insert_val);
        current->insert(child,index+1);
        child->parent = current;
    }
    
    else{
        VALUETYPE out_val = 0;
        Bnode_inner* new_node = current->split(out_val, insert_val, child);
        assert(out_val >0);
        
        if (!current->parent){
            Bnode_inner* new_parent = new Bnode_inner();
            
            new_parent->insert(out_val);
            new_parent->insert(current,0);
            new_parent->insert(new_node,1);
            
            current->parent = new_parent;
            new_node->parent = new_parent;
            
            root = new_parent;
        }
        
        else{
            current = current->parent;
            inner_node_insert(current, out_val, new_node);
        }
    }

}

void Btree::split_leaf_insert(Bnode_leaf* leaf_node, int insert_val){
    
    int num_val = leaf_node->getNumValues();
    assert( num_val == BTREE_LEAF_SIZE );
    
    Bnode_leaf* new_leaf_node = leaf_node->split(insert_val);
    VALUETYPE out_val = new_leaf_node->get(0);
    
    if ( leaf_node->parent == nullptr ){
        Bnode_inner* new_parent = new Bnode_inner();
        
        new_parent->insert(out_val);
        new_parent->insert(leaf_node,0);
        new_parent->insert(new_leaf_node,1);
        
        leaf_node->parent = new_parent;
        new_leaf_node->parent = new_parent;
        
        Bnode_leaf* rootIsLeaf = dynamic_cast<Bnode_leaf*>(root);
        assert(rootIsLeaf);
        
        root = new Bnode_inner(); // convert root from a leaf node to a inner_node
        
        root = new_parent;
        
        Bnode_inner* rootIsInner = dynamic_cast<Bnode_inner*>(root);
        assert(rootIsInner);
    }
    
    else{
        // Bnode_inner* parent_node = dynamic_cast<Bnode_inner*>(current->parent);  // did not work
    
        Bnode_inner* leaf_node_parent = new Bnode_inner();
        leaf_node_parent = leaf_node->parent;
        inner_node_insert( leaf_node_parent, out_val, new_leaf_node);
    }
}

void Btree::get_common_ancestor(int out_val, Bnode* lhs, Bnode* rhs){
    
//    Bnode_inner* lhs_par = lhs->parent;
//    Bnode_inner* rhs_par = rhs->parent;
    
    while (true){
        if(rhs->parent == lhs->parent){
            int index = lhs->parent->find_child(lhs);
            lhs->parent->replace_value(out_val, index);
            break;
        }
        else{
            rhs = rhs->parent;
            lhs = lhs->parent;
        }
    }
}

void Btree::inner_node_delete(Bnode_inner* parent, int index){
    
    if (index == -1){   //////// add corner case here
        parent->remove_value(0);
        parent->remove_child(0);
    }
    else{
        parent->remove_value(index);
        parent->remove_child(index + 1);
    }
    
    int num_val = parent->getNumValues();
    
    // if parent node is already root
    if(parent == root){
        if(num_val > 0) return;
        else{
            root = parent->getChild(0);
            
            root->parent = nullptr;
            
            // !!!!! check if the new root is a leaf node
            
            Bnode_leaf* Isleafnode = dynamic_cast<Bnode_leaf*>(root);
            
            if(Isleafnode){
                Isleafnode->prev = nullptr;
                Isleafnode->next = nullptr;
            }
            
            
            parent->clear();
            return;
        }
    }
    
    bool flag = 0;
    
    Bnode_inner* grad = parent->parent;
    int i = grad->find_child(parent);
    int grad_numval = grad->getNumValues();
    
    ///// consider if there is not left or right to this parent node
    
    Bnode_inner* left = nullptr;
    Bnode_inner* right = nullptr;
    
    if(i > 0)
         left = dynamic_cast<Bnode_inner*>(grad->getChild(i-1));
    if(i < grad_numval )
         right = dynamic_cast<Bnode_inner*>(grad->getChild(i+1));
    
    int left_numval = -1;
    int right_numval = -1;
    
    if(left)
        left_numval = left->getNumValues();
    if (right)
        right_numval = right->getNumValues();
    
    
    // delete directly
    if(num_val >= INNER_ORDER){   ////// changed from > to >=
        return;
    }
    
    
    // can not delete directly
    // left side
    if(!left){
        assert(right_numval >= 0);
        if(right_numval > INNER_ORDER) flag = 1;
        else flag = 0;
    }
    
    // right side
    else if(!right){
        assert(left_numval >= 0);
        if(left_numval > INNER_ORDER) flag = 1;
        else flag = 0;
    }
    
    // in the middle
    else{
        if( right_numval > INNER_ORDER) flag = 1;
        
        else if(left_numval > INNER_ORDER) flag = 1;
        
        //merge
        else flag = 0;
        
    }
    
    // redistribution
    if(flag){
        VALUETYPE new_parent_Val =  parent->redistribute(right, i); //// we need a return value here
        int parent_index = parent->parent->find_child(parent);
        parent->parent->replace_value(new_parent_Val, parent_index);
        return;
    }
    
    // merge
    else{
        
        int del_val;
        if (right)
            del_val = parent->merge(right, i);
        else
            del_val = left->merge(parent,i-1);
        
        int del_ind = grad->find_value_gt(del_val) - 1;
        inner_node_delete(grad, del_ind);
        return;
    }
    
}


//
// Given code
//
Data* Btree::search(VALUETYPE value) {
    assert(root);
    Bnode* current = root;

    // Have not reached a leaf node yet
    Bnode_inner* inner = dynamic_cast<Bnode_inner*>(current);
    // A dynamic cast <T> will return a nullptr if the given input is not polymorphically a T
    //                    will return a upcasted pointer to a T* if given input is polymorphically a T
    while (inner) {
        int find_index = inner->find_value_gt(value);
        current = inner->getChild(find_index);
        inner = dynamic_cast<Bnode_inner*>(current);
    }

    // Found a leaf node
    Bnode_leaf* leaf = dynamic_cast<Bnode_leaf*>(current);
    assert(leaf);
    for (int i = 0; i < leaf->getNumValues(); ++i) {
        if (leaf->get(i) > value)    return nullptr; // passed the possible location
        if (leaf->get(i) == value)   return leaf->getData(i);
    }

    // reached past the possible values - not here
    return nullptr;
}

