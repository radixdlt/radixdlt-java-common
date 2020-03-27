#!/bin/bash

ERROR_MSG_NO_SUCH_FOLDER="___folder_does_not_exist___"

RADIXDLT_JAVA_CLIENT_LIBRARY_NAME_OF_REPO_AND_ASSUMED_NAME_OF_DIRECTORY='radixdlt-java'
RADIXDLT_CORE_NAME_OF_REPO_AND_ASSUMED_NAME_OF_DIRECTORY='radixdlt-core'
RADIXDLT_ENGINE_NAME_OF_REPO_AND_ASSUMED_NAME_OF_DIRECTORY='radix-engine-library'
RADIXDLT_REGRESSION_TESTS_NAME_OF_REPO_AND_ASSUMED_NAME_OF_DIRECTORY='RadixRegressionTests' 

# Arguments:
# 	$1 = git_repo_absolute_path: String
# 	$2 = git_command: String
function git_command() {
	local git_repo_absolute_path="$1"
	local git_command="$2"
	echo $(git --git-dir="$git_repo_absolute_path" $git_command)
}

# Arguments:
# 	$1 = git_repo_absolute_path: String
# 	$1 = git_rev_parse_argument: String
function git_rev_parse_head() {
	local git_repo_absolute_path="$1"
	local git_rev_parse_argument="$2"
	echo $(git_command "$git_repo_absolute_path" "rev-parse $git_rev_parse_argument HEAD")
}

# Arguments:
# 	$1 = git_repo_absolute_path: String
function git_branch_name() {
	local git_repo_absolute_path="$1"
	echo $(git_rev_parse_head "$git_repo_absolute_path" --abbrev-ref)
}

# Arguments:
# 	$1 = git_repo_absolute_path: String
function git_last_commit_hash() {
	local git_repo_absolute_path="$1"
	echo $(git_rev_parse_head "$git_repo_absolute_path" --short)
}

# Arguments:
# 	$1 = git_repo_absolute_path: String
function git_summary_of_repo() {
	local git_repo_absolute_path="$1"
	if test -d "$git_repo_absolute_path"; then
		local branch_name=$(git_branch_name "$git_repo_absolute_path")
		local commit_hash=$(git_last_commit_hash "$git_repo_absolute_path")
		echo "#: $commit_hash, ᛋ: '$branch_name'"
	else
		echo "$ERROR_MSG_NO_SUCH_FOLDER"
	fi
}


echo "⚠️ This script assumes that all your Radix Java repos are placed" | tr -d '\n'
echo " in a shared root folder two directories up from this script," | tr -d '\n'
echo " with folders matching name of repos."


# assumes this scripts is in `<PATH-WITH-ALL-RADIX-GIT-REPOS>radixdlt-java-common/<SCRIPTS-DIR>/this_script.sh
script_dir=$(dirname "$0")
RADIXDLT_JAVA_COMMON_PATH="$script_dir/../"

RADIXDLT_JAVA_REPOS_PATH="$RADIXDLT_JAVA_COMMON_PATH/../"

RADIXDLT_JAVA_CLIENT_LIBRARY_PATH="$RADIXDLT_JAVA_REPOS_PATH/$RADIXDLT_JAVA_CLIENT_LIBRARY_NAME_OF_REPO_AND_ASSUMED_NAME_OF_DIRECTORY"
RADIXDLT_ENGINE_PATH="$RADIXDLT_JAVA_REPOS_PATH/$RADIXDLT_ENGINE_NAME_OF_REPO_AND_ASSUMED_NAME_OF_DIRECTORY"
RADIXDLT_CORE_PATH="$RADIXDLT_JAVA_REPOS_PATH/$RADIXDLT_CORE_NAME_OF_REPO_AND_ASSUMED_NAME_OF_DIRECTORY"
RADIXDLT_REGRESSION_TESTS_PATH="$RADIXDLT_JAVA_REPOS_PATH/$RADIXDLT_REGRESSION_TESTS_NAME_OF_REPO_AND_ASSUMED_NAME_OF_DIRECTORY"

radix_java_repos=( $RADIXDLT_JAVA_COMMON_PATH $RADIXDLT_JAVA_CLIENT_LIBRARY_PATH $RADIXDLT_ENGINE_PATH $RADIXDLT_CORE_PATH $RADIXDLT_REGRESSION_TESTS_PATH )

git_report=''

for radix_java_repo_path in "${radix_java_repos[@]}"
do
	git_repo_name=$(basename $radix_java_repo_path)
	if [[ "$git_repo_name" == ".." ]]; then
		# ugly fix for when using `basename` on the parent directory of this script
		# results in ".." as folder name.
		git_repo_name="radixdlt-java-common"
	fi
	git_repo_name_padded=$(printf "%-.25s %s\n" "${git_repo_name}                      ")
	git_info=$(git_summary_of_repo "$radix_java_repo_path/.git")
	if [[ "$git_info" == "$ERROR_MSG_NO_SUCH_FOLDER" ]]; then
		echo "$git_repo_name not found"
	else
		git_report="${git_report}$git_repo_name_padded $git_info\n"
	fi
done


echo "\n🔮Git status of Radix DLT Java repos:\n$git_report\n"