import Navbar from "./Navbar";

export const Header = () => {
  const logo = "/images/logo.png"; // Define your logo path or set to null if not needed
  return (
    <header className="flex items-center justify-end">
      {logo && (
        <img
          src={logo}
          alt="Gurukul-Tutor Logo"
          className="h-20 w-auto mr-4"
        />
      )}
      <Navbar />
    </header>
  )
}

