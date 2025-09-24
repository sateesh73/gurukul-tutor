import { Footer } from "~/components/Footer";
import type { Route } from "./+types/home";
import { Header } from "~/components/Header";
import { Hero } from "~/components/Hero";

export function meta({ }: Route.MetaArgs) {
  return [
    { title: "Gurukul" },
    { name: "description", content: "smart gurukul for tutoring" },
  ];
}

export default function Home() {
  return <main className="bg-[url('/images/bg-main.svg')] bg-cover p-10">
    <Header />
    <Hero />
    <Footer />
  </main>;
}
