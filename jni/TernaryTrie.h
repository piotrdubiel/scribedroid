#ifndef TERNARYTRIE_H_
#define TERNARYTRIE_H_

#include <map>
#include <vector>
#include <string>
#include <iostream>
#include "Trie.h"

class TernaryTrie : public Trie {
private:
	class Node {
	public:
		char character;
		Node* lowerChild;
		Node* higherChild;
		Node* equalChild;
		bool end;

		Node(char c) {
			character=c;
			lowerChild=NULL;
			higherChild=NULL;
			equalChild=NULL;
			end=false;
		}

		~Node() {
			delete lowerChild;
			delete higherChild;
			delete equalChild;
		}
	};

	Node* root;

	Node* insert(Node*,std::string);
	Node* search(std::string) const;
public:
	int count;

	TernaryTrie();
	~TernaryTrie();
	void dump() {
		std::cout<<"\t\t"<<root->character<<std::endl;
		std::cout<<"\t"<<root->lowerChild->character<<"\t"<<root->equalChild->character<<"\t"<<root->higherChild->character<<std::endl;
		Node* current=root->equalChild;
		std::cout<<"\t\t"<<current->equalChild->character<<std::endl;
		current=current->equalChild;
		std::cout<<"\t\t"<<current->equalChild->character<<std::endl;
	}
	void add(std::string);
	std::vector<std::string> suggest(std::string) const;
	std::vector<std::string> suggest(std::string,int) const;
	bool isValid(std::string) const;
};

#endif /* TERNARYTRIE_H_ */
