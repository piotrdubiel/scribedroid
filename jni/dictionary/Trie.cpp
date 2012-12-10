#include "trie.h"
#include <iostream>
#include <queue>
#include <utility>
//namespace scribedroid {

Trie::Trie() {
	root=new TrieNode('!');
	count=0;
}

Trie::~Trie() {}

void Trie::add(std::string word) {
	if (word.length()==0) return;
	TrieNode* currentNode=root;

	for (int i=0;i<word.length();++i) {
		if (currentNode->nodes.count(word[i])==0) {
			currentNode->nodes[word[i]]=new TrieNode(word[i]);
		}
		currentNode=currentNode->nodes[word[i]];

		if (i==word.length()-1 && currentNode->end==false) currentNode->end=true;
	}
	count++;
}

std::list<std::string> Trie::suggest(std::string prefix) {
	TrieNode* currentNode=getNodeForPrefix(prefix);
	std::list<std::string> endings;
	if (currentNode==NULL) {
		std::cout<<"No words"<<std::endl;
		return endings;
	}

	std::queue<std::pair<std::string,TrieNode*> > queue;
	queue.push(std::pair<std::string,TrieNode*> (prefix,currentNode));

	std::pair<std::string,TrieNode*> current;

	while (!queue.empty()) {
		current=queue.front();
		queue.pop();

		if (current.second->end==true) {
			endings.push_back(current.first);
		}

		for (std::map<char,TrieNode*>::iterator i=current.second->nodes.begin();i!=current.second->nodes.end();++i) {
			std::string str(current.first);
			str+=(*i).first;
			queue.push(std::pair<std::string,TrieNode*>(str,current.second->nodes[(*i).first]));
		}
	}
	return endings;
}

Trie::TrieNode* Trie::getNodeForPrefix(std::string prefix) const {
	TrieNode* currentNode=root;

	for (int i=0;i<prefix.length();++i) {
		if (currentNode->nodes.count(prefix[i])==0) return NULL;
		currentNode=currentNode->nodes[prefix[i]];
	}
	return currentNode;
}

void Trie::dump() {
	std::cout<<root->nodes.size()<<std::endl;
	for (char s='a';s<='z';s++) {
		if (root->nodes.count(s)>0)
			std::cout<<root->nodes[s]->character<<" "<<root->nodes[s]->end<<std::endl;
	}
}
//};
