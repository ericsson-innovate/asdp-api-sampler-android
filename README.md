# Getting Started

- [Download the Eclipse SDK](https://developer.android.com/sdk/index.html)
- Import the source code as an Eclipse project
 - File -> Import -> [Root of git repo]
 - Choose both "app" and "appcompat_v7"
 - There should be no errors in eclipse 

# Building 

- With Eclipse, it's built automatically
- With ant, goto the command line:  

  ant debug

# Deploy on a device

- With Eclipse (in the Project Explorer), select the LoginActivity -> File -> Run as

- With ant, goto the command line:  

  
  ant installd

