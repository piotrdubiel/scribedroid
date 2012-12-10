#include "TernaryTrie.h"
#include <queue>
#include <utility>

TernaryTrie::TernaryTrie() {
	root = NULL;
}

TernaryTrie::~TernaryTrie() {
	delete root;
}

void TernaryTrie::add(std::string word) {
	root=insert(root,word);
	count++;
}

TernaryTrie::Node* TernaryTrie::insert(TernaryTrie::Node* current,std::string word) {
	if (current == NULL) {
		current = new Node(word[0]);
	}
	if (word[0] < current->character) {
		current->lowerChild = insert(current->lowerChild, word);
	}
	else if (word[0] == current->character) {
		if (word.length()>1) {
			current->equalChild = insert(current->equalChild, word.substr(1,word.length()-1));
		}
		else {
			current->end=true;
		}
	} else {
		current->higherChild = insert(current->higherChild, word);
	}
	return current;
}

TernaryTrie::Node* TernaryTrie::search(std::string word) const {
	Node* current=root;
	unsigned int i=0;
	while (current != NULL) {
		if (word[i] < current->character) {
			current=current->lowerChild;
		}
		else if (word[i] == current->character) {
			i++;
			if (i == word.length()) {
				return current;
			}
			current = current->equalChild;
		}
		else {
			current = current->higherChild;
		}
	}
	return NULL;
}

bool TernaryTrie::isValid(std::string word) const {
	Node* n=search(word);
	return n!=NULL && n->end;
}

std::vector<std::string> TernaryTrie::suggest(std::string prefix,int limit) const {
	Node* currentNode=search(prefix);
	std::vector<std::string> results;
	if (currentNode == NULL) {
		return results;
	}

	std::queue<std::pair<std::string,Node*> > queue;

	if (currentNode->equalChild!=NULL) {
		queue.push(std::pair<std::string,Node*> (prefix,currentNode->equalChild));
	}
	else {
		return results;
	}

	std::pair<std::string,Node*> current;

	while (!queue.empty() && results.size()<limit) {
		current=queue.front();
		queue.pop();

		if (current.second!=NULL) {
			if (current.second->end) {
				results.push_back(current.first+current.second->character);
			}

			//Equal child
			if (current.second->equalChild!=NULL) {
				queue.push(std::pair<std::string,Node*>(current.first+current.second->character,current.second->equalChild));
			}
			//Lower child
			queue.push(std::pair<std::string,Node*>(current.first,current.second->lowerChild));
			//Higher child
			queue.push(std::pair<std::string,Node*>(current.first,current.second->higherChild));
		}
	}
	return results;
}

std::vector<std::string> TernaryTrie::suggest(std::string prefix) const {
	return suggest(prefix,count);
}
