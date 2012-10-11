#include "BasicTrie.h"
#include <iostream>
#include <queue>
#include <utility>
#include <fstream>

BasicTrie::BasicTrie() {
	root=new Node('!');
	count=0;
}

BasicTrie::~BasicTrie() {
	delete root;
}

BasicTrie::Node::~Node() {
	for (std::map<char,Node*>::const_iterator i=nodes.begin();i!=nodes.end();++i) {
		delete (*i).second;
	}
}

void BasicTrie::add(std::string word) {
	if (word.length()==0) return;
	Node* currentNode=root;

	for (int i=0;i<word.length();++i) {
		if (currentNode->nodes.count(word[i])==0) {
			currentNode->nodes[word[i]]=new Node(word[i]);
		}
		currentNode=currentNode->nodes[word[i]];

		if (i==word.length()-1 && currentNode->priority==-1) currentNode->priority=count;
	}
	count++;
}

std::vector<std::string> BasicTrie::suggest(std::string prefix) const {
	return suggest(prefix,count);
}

std::vector<std::string> BasicTrie::suggest(std::string prefix, int limit) const {
	Node* currentNode=search(prefix);
	std::vector<std::string> results;
	if (currentNode==NULL) {
		std::cout<<"No words"<<std::endl;
		return results;
	}

	std::queue<std::pair<std::string,Node*> > queue;
	queue.push(std::pair<std::string,Node*> (prefix,currentNode));

	std::pair<std::string,Node*> current;

	std::priority_queue<PriorityString, std::vector<PriorityString>, PriorityStringComp> strings;

	while (!queue.empty()) {
		current=queue.front();
		queue.pop();

		if (current.second->priority>=0) {
			strings.push(PriorityString(current.first, current.second->priority));
		}

		for (std::map<char,Node*>::const_iterator i=current.second->nodes.begin();i!=current.second->nodes.end();++i) {
			std::string str(current.first);
			str+=(*i).first;
			queue.push(std::pair<std::string,Node*>(str,current.second->nodes[(*i).first]));
		}
	}

	while (!strings.empty() && results.size() < limit) {
		results.push_back(strings.top().str);
		strings.pop();
	}

	return results;
}

bool BasicTrie::isValid(std::string word) const {
	Node* endNode=search(word);
	if (endNode==NULL) return false;
	return endNode->priority>=0;
}

void BasicTrie::load(std::ifstream& is) {

}

void BasicTrie::save(std::ofstream& os) const {
	os << "count="<<count;
	root->save(os);
}

void BasicTrie::Node::load(std::ifstream& is) {

}

void BasicTrie::Node::save(std::ofstream& os) const {
	os << "c=" << character;
	os << "p=" << priority;
	if (!nodes.empty()) {
		os << "<";
		for (std::map<char,Node*>::const_iterator i=nodes.begin();i!=nodes.end();++i) {
			(*i).second->save(os);
		}
		os<<">";
	}
}

BasicTrie::Node* BasicTrie::search(std::string prefix) const {
	Node* currentNode=root;

	for (int i=0;i<prefix.length();++i) {
		if (currentNode->nodes.count(prefix[i])==0) return NULL;
		currentNode=currentNode->nodes[prefix[i]];
	}
	return currentNode;
}

void BasicTrie::dump() {
	std::cout<<count<<std::endl;
	for (char s='a';s<='z';s++) {
		if (root->nodes.count(s)>0)
			std::cout<<root->nodes[s]->character<<" "<<root->nodes[s]->priority<<std::endl;
	}
}
//};
