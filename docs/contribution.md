Configure ssh connection based on git documentation [https://help.github.com/articles/connecting-to-github-with-ssh](https://help.github.com/articles/connecting-to-github-with-ssh).

1) **Fork repository** `https://github.com/qaprosoft/carina` to your own user.

2) **Clone your fork to your local machine**:

 `git clone git@github.com:your_fork_url/carina.git`

3) `git remote add origin <your_fork_url>` (can be already added)

4) `git fetch origin`

5) `git remote add upstream git@github.com:qaprosoft/carina.git`

6) `git fetch upstream`

7) `git checkout -b work_local_branch upstream/master`

And then after adding files (`git add` ...) use `git commit` (add description) and then`push`:

    git push origin work_local_branch:work_remote_branch
    
And on [https://github.com/qaprosoft/carina](https://github.com/qaprosoft/carina) you will see possibility to "Compare & Pull Request"
